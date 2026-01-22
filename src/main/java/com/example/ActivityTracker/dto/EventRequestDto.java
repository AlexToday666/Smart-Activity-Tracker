package com.example.ActivityTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class EventRequestDto {
    @Schema(description = "Идентификатор пользователя", example = "user-123", required = true)
    @NotBlank(message = "userId is required")
    private String userId;

    @Schema(description = "Тип события", example = "click", required = true)
    @NotBlank(message = "eventType is required")
    private String eventType;

    @Schema(description = "Произвольные метаданные в формате JSON (строка)", example = "{\"button\":\"buy\"}")
    private String metadata;
    @Schema(description = "Время события (ISO-8601). Если не указано, устанавливается текущее время",
            example = "2026-01-21T12:00:00Z")
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
