package com.example.ActivityTracker.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public class EventResponseDto {

    @Schema(description = "Идентификатор события", example = "1")
    private Long id;

    private Long projectId;

    private String eventId;

    @Schema(description = "Идентификатор пользователя", example = "user-123")
    private String userId;

    @Schema(description = "Тип события", example = "click")
    private String type;

    @Schema(description = "Метаданные события", example = "{\"button\":\"buy\"}")
    private JsonNode metadata;

    @Schema(description = "Время события", example = "2026-01-21T12:00:00Z")
    private Instant occurredAt;

    private Instant receivedAt;

    private String source;

    private String sessionId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEventType() {
        return type;
    }

    public void setEventType(String eventType) {
        this.type = eventType;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getEventTime() {
        return occurredAt;
    }

    public void setEventTime(Instant eventTime) {
        this.occurredAt = eventTime;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
