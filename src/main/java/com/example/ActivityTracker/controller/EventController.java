package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.dto.EventRequestDto;
import com.example.ActivityTracker.dto.EventResponseDto;
import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.service.EventService;
import com.example.ActivityTracker.mapper.EventMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventController(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @GetMapping
    public Page<EventResponseDto> getAllEvents(Pageable pageable){
        return eventService.getAllEvents(pageable)
                .map(eventMapper::toDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(eventMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody EventRequestDto dto) {
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
