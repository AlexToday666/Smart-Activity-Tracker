package com.example.ActivityTracker.service;

import com.example.ActivityTracker.dto.EventTypeCount;
import com.example.ActivityTracker.exception.NotFoundException;
import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.repository.EventRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }


    public Event createEvent(Event event) {
        if (event.getEventTime() == null) {
            event.setEventTime(Instant.now());
        }
        return eventRepository.save(event);
    }


    @Transactional(readOnly = true)
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsByUser(String userId, Pageable pageable) {
        return eventRepository.findByUserIdOrderByEventTimeDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsBetween(Instant from, Instant to, Pageable pageable) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        return eventRepository.findByEventTimeBetweenOrderByEventTimeDesc(from, to, pageable);
    }


    public Event updateEvent(Long id, Event updated) {
        Event existing = getEventById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        existing.setEventType(updated.getEventType());
        existing.setMetadata(updated.getMetadata());
        existing.setEventTime(updated.getEventTime());

        return eventRepository.save(existing);
    }


    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new NotFoundException("Event not found: " + id);
        }
        eventRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public List<EventTypeCount> countByEventTypeBetween(Instant from, Instant to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        return eventRepository.countByEventTypeBetween(from, to);
    }

    @Transactional(readOnly = true)
    public long countDistinctUsersBetween(Instant from, Instant to) {
        return eventRepository.countDistinctUsersBetween(from, to);
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsByEventType(String eventType, Pageable pageable) {
        return eventRepository.findByEventTypeOrderByEventTimeDesc(eventType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsByUserAndBetween(
            String userId,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        return eventRepository.findByUserIdAndEventTimeBetweenOrderByEventTimeDesc(userId, from, to, pageable);
    }
}
