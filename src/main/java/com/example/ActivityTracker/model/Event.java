package com.example.ActivityTracker.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_events_project_occurred_at", columnList = "project_id, occurred_at"),
        @Index(name = "idx_events_project_type", columnList = "project_id, event_type"),
        @Index(name = "idx_events_project_user_id", columnList = "project_id, user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_events_project_event_id", columnNames = {"project_id", "event_id"})
})

public class Event {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    private String source;

    @Column(name = "session_id")
    private String sessionId;

    @PrePersist
    void onCreate() {
        this.receivedAt = Instant.now();
        if (this.occurredAt == null) {
            this.occurredAt = this.receivedAt;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Long getProjectId() {
        return project == null ? null : project.getId();
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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
}
