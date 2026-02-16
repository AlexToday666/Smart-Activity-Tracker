package com.example.ActivityTracker.dto;

public record FunnelStepDto(String step, long users, double conversionFromPrevious, double conversionFromStart) {
}
