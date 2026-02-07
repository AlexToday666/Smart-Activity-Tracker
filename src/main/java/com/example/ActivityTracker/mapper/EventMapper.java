package com.example.ActivityTracker.mapper;


import com.example.ActivityTracker.dto.EventRequestDto;
import com.example.ActivityTracker.dto.EventResponseDto;
import com.example.ActivityTracker.model.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    // DTO to Entity
    public Event toEntity(EventRequestDto dto) {

        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setEventId(dto.getEventId());
        event.setUserId(dto.getUserId());
        event.setEventType(dto.getType());
        event.setMetadata(dto.getMetadata());
        event.setOccurredAt(dto.getOccurredAt());
        event.setSource(dto.getSource());
        event.setSessionId(dto.getSessionId());

        return event;
    }

    // Entity to DTO
    public EventResponseDto toDto(Event event) {
        if (event == null) {
            return null;
        }
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setProjectId(event.getProjectId());
        dto.setEventId(event.getEventId());
        dto.setUserId(event.getUserId());
        dto.setType(event.getEventType());
        dto.setMetadata(event.getMetadata());
        dto.setOccurredAt(event.getOccurredAt());
        dto.setReceivedAt(event.getReceivedAt());
        dto.setSource(event.getSource());
        dto.setSessionId(event.getSessionId());

        return dto;
    }
}
