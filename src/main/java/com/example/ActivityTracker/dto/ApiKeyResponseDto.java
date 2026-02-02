package com.example.ActivityTracker.dto;

import java.time.Instant;

public record ApiKeyResponseDto(
        Long id,
        Long projectId,
        String name,
        boolean active,
        Instant createdAt,
        Instant lastUsedAt,
        Instant revokedAt
) {
}
