package com.example.ActivityTracker.dto;

import java.time.Instant;
import java.util.List;

public record TopEventTypesResponseDto(Long projectId, Instant from, Instant to, List<EventTypeCount> eventTypes) {
}
