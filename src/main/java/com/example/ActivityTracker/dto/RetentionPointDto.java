package com.example.ActivityTracker.dto;

public record RetentionPointDto(int day, long cohortUsers, long retainedUsers, double retentionRate) {
}
