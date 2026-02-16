package com.example.ActivityTracker.dto;

import java.time.Instant;
import java.util.List;

public record CohortResponseDto(Long projectId, Instant from, Instant to, List<CohortDto> cohorts) {
}
