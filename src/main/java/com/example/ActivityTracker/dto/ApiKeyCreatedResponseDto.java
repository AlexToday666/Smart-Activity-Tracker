package com.example.ActivityTracker.dto;

public record ApiKeyCreatedResponseDto(
        ApiKeyResponseDto key,
        String secret
) {
}
