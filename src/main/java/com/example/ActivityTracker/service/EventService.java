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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
        return eventRepository.findAll(withDefaultSort(pageable));
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsByUser(String userId, Pageable pageable) {
        return eventRepository.findByUserIdOrderByEventTimeDesc(userId, withDefaultSort(pageable));
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsBetween(Instant from, Instant to, Pageable pageable) {
        validateTimeRange(from, to);
        return eventRepository.findByEventTimeBetweenOrderByEventTimeDesc(from, to, withDefaultSort(pageable));
    }


    public Event updateEvent(Long id, Event updated) {
        Event existing = getEventById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        existing.setEventType(updated.getEventType());
        existing.setMetadata(updated.getMetadata());
        if (updated.getEventTime() != null) {
            existing.setEventTime(updated.getEventTime());
        }

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
        validateTimeRange(from, to);
        return eventRepository.countByEventTypeBetween(from, to);
    }

    @Transactional(readOnly = true)
    public long countDistinctUsersBetween(Instant from, Instant to) {
        validateTimeRange(from, to);
        return eventRepository.countDistinctUsersBetween(from, to);
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsByEventType(String eventType, Pageable pageable) {
        return eventRepository.findByEventTypeOrderByEventTimeDesc(eventType, withDefaultSort(pageable));
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsByUserAndBetween(
            String userId,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        validateTimeRange(from, to);
        return eventRepository.findByUserIdAndEventTimeBetweenOrderByEventTimeDesc(
                userId,
                from,
                to,
                withDefaultSort(pageable)
        );
    }

    @Transactional(readOnly = true)
    public Page<Event> getEvents(
            String userId,
            String eventType,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        if (from != null || to != null) {
            validateTimeRange(from, to);
        }

        Specification<Event> specification = Specification.where(null);
        if (userId != null && !userId.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("userId"), userId));
        }
        if (eventType != null && !eventType.isBlank()) {
            specification = specification.and(
                    (root, query, builder) -> builder.equal(root.get("eventType"), eventType)
            );
        }
        if (from != null && to != null) {
            specification = specification.and((root, query, builder) -> builder.between(root.get("eventTime"), from, to));
        }

        return eventRepository.findAll(specification, withDefaultSort(pageable));
    }

    private void validateTimeRange(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to are required");
        }
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
    }

    private Pageable withDefaultSort(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return pageable;
        }
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return Pageable.ofSize(pageable.getPageSize())
                .withPage(pageable.getPageNumber())
                .withSort(Sort.by(Sort.Direction.DESC, "eventTime"));
    }
}
