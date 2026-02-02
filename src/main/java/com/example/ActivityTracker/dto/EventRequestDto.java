package com.example.ActivityTracker.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class EventRequestDto {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Schema(description = "Client-side event idempotency key", example = "evt-2026-0001", required = true)
    @NotBlank(message = "eventId is required")
    @Size(max = 120, message = "eventId must be at most 120 characters")
    private String eventId;

    @Schema(description = "Идентификатор пользователя", example = "user-123", required = true)
    @NotBlank(message = "userId is required")
    @Size(max = 120, message = "userId must be at most 120 characters")
    private String userId;

    @Schema(description = "Тип события", example = "purchase", required = true)
    @NotBlank(message = "type is required")
    @Size(max = 120, message = "type must be at most 120 characters")
    private String type;

    @Schema(description = "Произвольные метаданные JSON", example = "{\"country\":\"DE\",\"device\":\"mobile\"}")
    private JsonNode metadata;

    @Schema(description = "Source system or SDK", example = "web")
    @Size(max = 80, message = "source must be at most 80 characters")
    private String source;

    @Schema(description = "Session identifier when known", example = "sess-42")
    @Size(max = 160, message = "sessionId must be at most 160 characters")
    private String sessionId;

    @Schema(description = "Время события (ISO-8601). Если не указано, устанавливается текущее время",
            example = "2026-01-21T12:00:00Z")
    private Instant occurredAt;

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

    public void setMetadata(String metadata) {
        if (metadata == null) {
            this.metadata = null;
            return;
        }
        try {
            this.metadata = OBJECT_MAPPER.readTree(metadata);
        } catch (Exception ex) {
            this.metadata = TextNode.valueOf(metadata);
        }
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
}
