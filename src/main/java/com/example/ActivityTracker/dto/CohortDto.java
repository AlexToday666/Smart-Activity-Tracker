package com.example.ActivityTracker.dto;

import java.time.LocalDate;

public record CohortDto(LocalDate cohortDate, long users) {
}
