package com.example.ActivityTracker.dto;

import java.time.LocalDate;

public record ActiveUsersPointDto(LocalDate date, long users) {
}
