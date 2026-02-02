package com.example.ActivityTracker.dto;

import java.time.Instant;

public record ProjectResponseDto(
        Long id,
        String name,
        String slug,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
