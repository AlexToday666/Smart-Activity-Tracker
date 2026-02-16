package com.example.ActivityTracker.dto;

import java.time.Instant;
import java.util.List;

public record FunnelResponseDto(
        Long projectId,
        Instant from,
        Instant to,
        List<String> steps,
        List<FunnelStepDto> results
) {
}
