package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.dto.EventRequestDto;
import com.example.ActivityTracker.dto.EventResponseDto;
import com.example.ActivityTracker.exception.BadRequestException;
import com.example.ActivityTracker.mapper.EventMapper;
import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.service.EventService;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventController(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(eventMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Page<EventResponseDto> getEvents(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable
    ) {
        if ((from == null) != (to == null)) {
            throw new BadRequestException("from and to must be provided together");
        }
        Page<Event> events;
        if (userId != null && from != null) {
            events = eventService.getEventsByUserAndBetween(userId, from, to, pageable);
        } else if (userId != null) {
            events = eventService.getEventsByUser(userId, pageable);
        } else if (eventType != null) {
            events = eventService.getEventsByEventType(eventType, pageable);
        } else if (from != null) {
            events = eventService.getEventsBetween(from, to, pageable);
        } else {
            events = eventService.getAllEvents(pageable);
        }

        return events.map(eventMapper::toDto);
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody @Valid EventRequestDto dto) {
        Event event = eventMapper.toEntity(dto);
        Event savedEvent = eventService.createEvent(event);
        return ResponseEntity.ok(eventMapper.toDto(savedEvent));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable Long id,
                                                        @RequestBody @Valid EventRequestDto dto) {
        Event event = eventMapper.toEntity(dto);
        Event updateEvent = eventService.updateEvent(id, event);
        return ResponseEntity.ok(eventMapper.toDto(updateEvent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
