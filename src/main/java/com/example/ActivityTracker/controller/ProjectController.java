package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.dto.ApiKeyCreateRequestDto;
import com.example.ActivityTracker.dto.ApiKeyCreatedResponseDto;
import com.example.ActivityTracker.dto.ApiKeyResponseDto;
import com.example.ActivityTracker.dto.ProjectRequestDto;
import com.example.ActivityTracker.dto.ProjectResponseDto;
import com.example.ActivityTracker.model.ApiKey;
import com.example.ActivityTracker.model.Project;
import com.example.ActivityTracker.service.ApiKeyService;
import com.example.ActivityTracker.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/projects", "/api/v1/projects"})
public class ProjectController {
    private final ProjectService projectService;
    private final ApiKeyService apiKeyService;

    public ProjectController(ProjectService projectService, ApiKeyService apiKeyService) {
        this.projectService = projectService;
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDto> create(@RequestBody @Valid ProjectRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(projectService.create(request)));
    }

    @GetMapping
    public List<ProjectResponseDto> list() {
        return projectService.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public ProjectResponseDto get(@PathVariable Long id) {
        return toDto(projectService.get(id));
    }

    @PutMapping("/{id}")
    public ProjectResponseDto update(@PathVariable Long id, @RequestBody @Valid ProjectRequestDto request) {
        return toDto(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/api-keys")
    public ResponseEntity<ApiKeyCreatedResponseDto> createApiKey(
            @PathVariable Long projectId,
            @RequestBody @Valid ApiKeyCreateRequestDto request
    ) {
        ApiKeyService.CreatedApiKey created = apiKeyService.create(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiKeyCreatedResponseDto(toDto(created.apiKey()), created.secret()));
    }

    @GetMapping("/{projectId}/api-keys")
    public List<ApiKeyResponseDto> listApiKeys(@PathVariable Long projectId) {
        return apiKeyService.list(projectId).stream().map(this::toDto).toList();
    }

    @PostMapping("/{projectId}/api-keys/{apiKeyId}/revoke")
    public ApiKeyResponseDto revokeApiKey(@PathVariable Long projectId, @PathVariable Long apiKeyId) {
        return toDto(apiKeyService.revoke(projectId, apiKeyId));
    }

    private ProjectResponseDto toDto(Project project) {
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getSlug(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private ApiKeyResponseDto toDto(ApiKey apiKey) {
        return new ApiKeyResponseDto(
                apiKey.getId(),
                apiKey.getProject().getId(),
                apiKey.getName(),
                apiKey.isActive(),
                apiKey.getCreatedAt(),
                apiKey.getLastUsedAt(),
                apiKey.getRevokedAt()
        );
    }
}
