package com.example.ActivityTracker.service;

import com.example.ActivityTracker.dto.BatchIngestResponseDto;
import com.example.ActivityTracker.dto.BatchItemErrorDto;
import com.example.ActivityTracker.dto.EventTypeCount;
import com.example.ActivityTracker.exception.BadRequestException;
import com.example.ActivityTracker.exception.NotFoundException;
import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.model.Project;
import com.example.ActivityTracker.repository.EventRepository;
import com.example.ActivityTracker.repository.ProjectRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.criteria.Expression;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventService {
    private static final Pattern METADATA_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;
    private final Counter ingestedCounter;
    private final Counter duplicateCounter;
    private final Counter rejectedCounter;
    private final DistributionSummary batchSizeSummary;
    private final Timer batchTimer;

    public EventService(
            EventRepository eventRepository,
            ProjectRepository projectRepository,
            MeterRegistry meterRegistry
    ) {
        MeterRegistry registry = meterRegistry == null ? new SimpleMeterRegistry() : meterRegistry;
        this.eventRepository = eventRepository;
        this.projectRepository = projectRepository;
        this.ingestedCounter = Counter.builder("events.ingested.total").register(registry);
        this.duplicateCounter = Counter.builder("events.duplicate.total").register(registry);
        this.rejectedCounter = Counter.builder("events.rejected.total").register(registry);
        this.batchSizeSummary = DistributionSummary.builder("events.batch.size").register(registry);
        this.batchTimer = Timer.builder("events.batch.duration").register(registry);
    }

    EventService(EventRepository eventRepository) {
        this(eventRepository, null, new SimpleMeterRegistry());
    }


    @Deprecated
    public Event createEvent(Event event) {
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(Instant.now());
        }
        return eventRepository.save(event);
    }

    public IngestOutcome ingest(Project project, Event event) {
        event.setProject(project);
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(Instant.now());
        }
        Optional<Event> existing = eventRepository.findByProjectIdAndEventId(project.getId(), event.getEventId());
        if (existing.isPresent()) {
            duplicateCounter.increment();
            return new IngestOutcome(existing.get(), true);
        }
        Event saved = eventRepository.save(event);
        ingestedCounter.increment();
        return new IngestOutcome(saved, false);
    }

    public BatchIngestResponseDto recordBatch(int total, int accepted, int duplicated, List<BatchItemErrorDto> errors) {
        return batchTimer.record(() -> {
            batchSizeSummary.record(total);
            int rejected = errors.size();
            if (rejected > 0) {
                rejectedCounter.increment(rejected);
            }
            return new BatchIngestResponseDto(total, accepted, duplicated, rejected, errors);
        });
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
        return eventRepository.findByUserIdOrderByOccurredAtDesc(userId, withDefaultSort(pageable));
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsBetween(Instant from, Instant to, Pageable pageable) {
        validateTimeRange(from, to);
        return eventRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(from, to, withDefaultSort(pageable));
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
    public List<EventTypeCount> countByEventTypeBetween(Long projectId, Instant from, Instant to) {
        validateTimeRange(from, to);
        return eventRepository.countByEventTypeBetween(projectId, from, to);
    }

    @Transactional(readOnly = true)
    public long countDistinctUsersBetween(Instant from, Instant to) {
        validateTimeRange(from, to);
        return eventRepository.countDistinctUsersBetween(from, to);
    }

    @Transactional(readOnly = true)
    public long countDistinctUsersBetween(Long projectId, Instant from, Instant to) {
        validateTimeRange(from, to);
        return eventRepository.countDistinctUsersBetween(projectId, from, to);
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsByEventType(String eventType, Pageable pageable) {
        return eventRepository.findByEventTypeOrderByOccurredAtDesc(eventType, withDefaultSort(pageable));
    }

    @Transactional(readOnly = true)
    public Page<Event> getEventsByUserAndBetween(
            String userId,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        validateTimeRange(from, to);
        return eventRepository.findByUserIdAndOccurredAtBetweenOrderByOccurredAtDesc(
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
        return getEvents(null, userId, eventType, from, to, null, null, Map.of(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Event> getEvents(
            Long projectId,
            String userId,
            String eventType,
            Instant from,
            Instant to,
            String source,
            String sessionId,
            Map<String, String> metadataFilters,
            Pageable pageable
    ) {
        if (from != null || to != null) {
            validateTimeRange(from, to);
        }
        validateMetadataFilters(metadataFilters);

        Specification<Event> specification = Specification.where(null);
        if (projectId != null) {
            specification = specification.and(
                    (root, query, builder) -> builder.equal(root.get("project").get("id"), projectId)
            );
        }
        if (userId != null && !userId.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("userId"), userId));
        }
        if (eventType != null && !eventType.isBlank()) {
            specification = specification.and(
                    (root, query, builder) -> builder.equal(root.get("eventType"), eventType)
            );
        }
        if (from != null && to != null) {
            specification = specification.and(
                    (root, query, builder) -> builder.between(root.get("occurredAt"), from, to)
            );
        }
        if (source != null && !source.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("source"), source));
        }
        if (sessionId != null && !sessionId.isBlank()) {
            specification = specification.and(
                    (root, query, builder) -> builder.equal(root.get("sessionId"), sessionId)
            );
        }
        for (Map.Entry<String, String> filter : metadataFilters.entrySet()) {
            specification = specification.and((root, query, builder) -> {
                Expression<String> value = builder.function(
                        "jsonb_extract_path_text",
                        String.class,
                        root.get("metadata"),
                        builder.literal(filter.getKey())
                );
                return builder.equal(value, filter.getValue());
            });
        }

        return eventRepository.findAll(specification, withDefaultSort(pageable));
    }

    public void validateTimeRange(Instant from, Instant to) {
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
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "occurredAt")
        );
    }

    private void validateMetadataFilters(Map<String, String> metadataFilters) {
        List<String> invalid = new ArrayList<>();
        for (String key : metadataFilters.keySet()) {
            if (!METADATA_KEY_PATTERN.matcher(key).matches()) {
                invalid.add(key);
            }
        }
        if (!invalid.isEmpty()) {
            throw new BadRequestException("Invalid metadata filter key(s): " + String.join(", ", invalid));
        }
    }

    public record IngestOutcome(Event event, boolean duplicated) {
    }
}
