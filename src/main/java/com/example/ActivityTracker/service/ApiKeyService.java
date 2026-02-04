package com.example.ActivityTracker.service;

import com.example.ActivityTracker.dto.ApiKeyCreateRequestDto;
import com.example.ActivityTracker.exception.NotFoundException;
import com.example.ActivityTracker.exception.UnauthorizedException;
import com.example.ActivityTracker.model.ApiKey;
import com.example.ActivityTracker.model.Project;
import com.example.ActivityTracker.repository.ApiKeyRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApiKeyService {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;
    private final ProjectService projectService;
    private final Counter usageCounter;
    private final Counter authFailureCounter;

    public ApiKeyService(
            ApiKeyRepository apiKeyRepository,
            ProjectService projectService,
            MeterRegistry meterRegistry
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.projectService = projectService;
        this.usageCounter = Counter.builder("api.keys.usage.total").register(meterRegistry);
        this.authFailureCounter = Counter.builder("authentication.failures.total").register(meterRegistry);
    }

    public CreatedApiKey create(Long projectId, ApiKeyCreateRequestDto request) {
        Project project = projectService.get(projectId);
        String secret = generateSecret();

        ApiKey apiKey = new ApiKey();
        apiKey.setProject(project);
        apiKey.setName(request.getName());
        apiKey.setKeyHash(hash(secret));

        ApiKey saved = apiKeyRepository.save(apiKey);
        log.info("api_key_created projectId={} apiKeyId={} name={}", projectId, saved.getId(), saved.getName());
        return new CreatedApiKey(saved, secret);
    }

    @Transactional(readOnly = true)
    public List<ApiKey> list(Long projectId) {
        projectService.get(projectId);
        return apiKeyRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public ApiKey revoke(Long projectId, Long apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .filter(key -> key.getProject().getId().equals(projectId))
                .orElseThrow(() -> new NotFoundException("API key not found: " + apiKeyId));
        apiKey.revoke();
        ApiKey saved = apiKeyRepository.save(apiKey);
        log.info("api_key_revoked projectId={} apiKeyId={}", projectId, apiKeyId);
        return saved;
    }

    public ApiKey authenticate(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            authFailureCounter.increment();
            log.warn("api_key_auth_failed reason=missing");
            throw new UnauthorizedException("X-API-Key header is required");
        }
        ApiKey apiKey = apiKeyRepository.findByKeyHashAndActiveTrue(hash(rawApiKey))
                .orElseThrow(() -> {
                    authFailureCounter.increment();
                    log.warn("api_key_auth_failed reason=invalid");
                    return new UnauthorizedException("Invalid API key");
                });
        apiKey.markUsed();
        usageCounter.increment();
        MDC.put("projectId", String.valueOf(apiKey.getProject().getId()));
        MDC.put("apiKeyId", String.valueOf(apiKey.getId()));
        return apiKey;
    }

    public String hash(String rawApiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawApiKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return "sat_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record CreatedApiKey(ApiKey apiKey, String secret) {
    }
}
