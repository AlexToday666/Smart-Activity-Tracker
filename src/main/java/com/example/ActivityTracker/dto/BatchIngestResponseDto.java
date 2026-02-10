package com.example.ActivityTracker.dto;

import java.util.List;

public record BatchIngestResponseDto(
        int total,
        int accepted,
        int duplicated,
        int rejected,
        List<BatchItemErrorDto> errors
) {
}
