package com.example.ActivityTracker.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class EventRequestDto {
    @NotBlank(message = "userId is required")
    private String userId;
    @NotBlank(message = "eventType is required")
    private String eventType;
    private String metadata;
    private Instant eventTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }
}
