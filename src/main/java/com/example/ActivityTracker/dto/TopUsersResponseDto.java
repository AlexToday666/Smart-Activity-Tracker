package com.example.ActivityTracker.dto;

import java.time.Instant;
import java.util.List;

public record TopUsersResponseDto(Long projectId, Instant from, Instant to, List<TopUserDto> users) {
}
