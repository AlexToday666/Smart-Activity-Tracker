package com.example.ActivityTracker.repository;

import com.example.ActivityTracker.model.ApiKey;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash);

    List<ApiKey> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
