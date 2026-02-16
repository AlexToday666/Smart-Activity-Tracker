package com.example.ActivityTracker.dto;

import java.time.Instant;

public record SessionAnalyticsResponseDto(
        Long projectId,
        Instant from,
        Instant to,
        long sessions,
        long users,
        double averageEventsPerSession,
        double averageDurationSeconds,
        String strategy
) {
}
