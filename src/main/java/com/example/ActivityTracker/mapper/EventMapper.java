package com.example.ActivityTracker.mapper;


import com.example.ActivityTracker.dto.EventResponseDto;
import com.example.ActivityTracker.model.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    // DTO to Entity
    public Event toEntity(EventResponseDto dto) {

        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setUserId(dto.getUserId());
        event.setEventType(dto.getEventType());
        event.setMetadata(dto.getMetadata());
        event.setEventTime(dto.getEventTime());

        return event;
    }

    // Entity to DTO
    public EventResponseDto toDto(Event event) {
        if (event == null) {
            return null;
        }
        EventResponseDto dto = new EventResponseDto();
        event.setId(event.getId());
        event.setUserId(event.getUserId());
        event.setEventType(event.getEventType());
        event.setMetadata(event.getMetadata());
        event.setEventTime(event.getEventTime());
        event.setCreatedAt(event.getCreatedAt());

        return dto;
    }
}
