package com.example.ActivityTracker.dto;

public record IngestResultDto(EventResponseDto event, boolean duplicated) {
}
