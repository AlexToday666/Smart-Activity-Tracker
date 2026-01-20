package com.example.ActivityTracker.service;


import com.example.ActivityTracker.exception.NotFoundException;
import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void createEvent_setsEventTimeWhenMissing() {
        Event event = new Event();
        event.setUserId("user-1");
        event.setEventType("login");

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Event saved = eventService.createEvent(event);

        assertNotNull(saved.getEventTime());

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass((Event.class));
        verify(eventRepository).save(captor.capture());
        assertNotNull(captor.getValue().getEventTime());
    }

    @Test
    void getEventsBetween_throwsWhenFromAfterTo() {
        Instant from = Instant.parse("2024-02-02T00:00:00Z");
        Instant to = Instant.parse("2024-02-02T00:00:00Z");

        assertThrows(IllegalArgumentException.class,
                () -> eventService.getEventsBetween(from, to, Pageable.unpaged()));
    }

    @Test
    void getEventsByUser_delegatesToRepository() {
        when(eventRepository.findByUserIdOrderByEventTimeDesc("user-1", Pageable.unpaged()))
                .thenReturn(Page.empty());
        Page<Event> result = eventService.getEventsByUser("user-1", Pageable.unpaged());
        assertEquals(0, result.getTotalElements());
        verify(eventRepository).findByEventTypeOrderByEventTimeDesc("user-1", Pageable.unpaged());
    }

    @Test
    void updateEvent_throwsWhenNotFound() {
        when(eventRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> eventService.updateEvent(42L, new Event()));
    }

    @Test
    void updateEvent_updatesFields() {
        Event existing = new Event();
        existing.setId(1L);
        existing.setEventType("old");
        existing.setMetadata("old-meta");
        existing.setEventTime(Instant.parse("2024-01-01T00:00:00Z"));

        Event updated = new Event();
        updated.setEventType("new");
        updated.setMetadata("new-meta");
        updated.setEventTime(Instant.parse("2024-02-01T00:00:00Z"));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.updateEvent(1L, updated);

        assertEquals("new", result.getEventType());
        assertEquals("new-meta", result.getMetadata());
        assertEquals(Instant.parse("2024-02-01T00:00:00Z"), result.getEventTime());
    }

    @Test
    void deleteEvent_throwsWhenMissing() {
        when(eventRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> eventService.deleteEvent(99L));
    }

    @Test
    void countByEventTypeBetween_throwsWhenFromAfterTo() {
        Instant from = Instant.parse("2024-02-02T00:00:00Z");
        Instant to = Instant.parse("2024-02-01T00:00:00Z");

        assertThrows(IllegalArgumentException.class,
                () -> eventService.countByEventTypeBetween(from, to));
    }
}
