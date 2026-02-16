package com.example.ActivityTracker.dto;

import java.time.Instant;
import java.util.List;

public record RetentionResponseDto(Long projectId, Instant from, Instant to, List<RetentionPointDto> days) {
}
