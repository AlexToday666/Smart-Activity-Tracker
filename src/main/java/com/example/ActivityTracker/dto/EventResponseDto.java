package com.example.ActivityTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public class EventResponseDto {

    @Schema(description = "Идентификатор события", example = "1")
    private Long id;

    @Schema(description = "Идентификатор пользователя", example = "user-123")
    private String userId;

    @Schema(description = "Тип события", example = "click")
    private String eventType;

    @Schema(description = "Метаданные события", example = "{\"button\":\"buy\"}")
    private String metadata;

    @Schema(description = "Время события", example = "2026-01-21T12:00:00Z")
    private Instant eventTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
