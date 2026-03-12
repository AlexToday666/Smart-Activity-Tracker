package com.example.ActivityTracker.dto;

import java.time.Instant;

public class DauResponseDto {
    private final Instant from;
    private final Instant to;
    private final long dau;

    public DauResponseDto(Instant from, Instant to, long dau) {
        this.from = from;
        this.to = to;
        this.dau = dau;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public long getDau() {
        return dau;
    }
}
