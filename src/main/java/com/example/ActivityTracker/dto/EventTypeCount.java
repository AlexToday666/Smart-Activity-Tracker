package com.example.ActivityTracker.dto;

public class EventTypeCount {
    private final String eventType;
    private final long count;

    public EventTypeCount(String eventType, long count) {
        this.eventType = eventType;
        this.count = count;
    }

    public String getEventType() {
        return eventType;
    }

    public long getCount() {
        return count;
    }
}
