package com.example.ActivityTracker.dto;

import java.time.Instant;
import java.util.List;

public record ActiveUsersResponseDto(
        Long projectId,
        Instant from,
        Instant to,
        String granularity,
        List<ActiveUsersPointDto> points
) {
}
