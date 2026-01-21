package com.example.ActivityTracker.dto;

import java.time.Instant;

public class DauResponseDto {
    private final Instant from;
    private final Instant to;
    private final long uniqueUsers;

    public DauResponseDto(Instant from, Instant to, long uniqueUsers) {
        this.from = from;
        this.to = to;
        this.uniqueUsers = uniqueUsers;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public long getUniqueUsers() {
        return uniqueUsers;
    }
}
