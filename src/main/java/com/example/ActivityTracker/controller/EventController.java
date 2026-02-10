package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.dto.BatchIngestResponseDto;
import com.example.ActivityTracker.dto.BatchItemErrorDto;
import com.example.ActivityTracker.dto.EventRequestDto;
import com.example.ActivityTracker.dto.EventResponseDto;
import com.example.ActivityTracker.dto.IngestResultDto;
import com.example.ActivityTracker.exception.BadRequestException;
import com.example.ActivityTracker.mapper.EventMapper;
import com.example.ActivityTracker.model.ApiKey;
import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.service.ApiKeyService;
import com.example.ActivityTracker.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Events", description = "Project-scoped event ingestion and querying")
@RestController
@RequestMapping({"/api/events", "/api/v1/events"})
public class EventController {
    private static final Logger log = LoggerFactory.getLogger(EventController.class);
    private static final int MAX_BATCH_SIZE = 500;

    private final EventService eventService;
    private final EventMapper eventMapper;
    private final ApiKeyService apiKeyService;
    private final Validator validator;

    public EventController(
            EventService eventService,
            EventMapper eventMapper,
            ApiKeyService apiKeyService,
            Validator validator
    ) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
        this.apiKeyService = apiKeyService;
        this.validator = validator;
    }

    @Operation(summary = "Get event by database id")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(eventMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search events with metadata-aware filters")
    @GetMapping
    public Page<EventResponseDto> getEvents(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam MultiValueMap<String, String> params,
            Pageable pageable
    ) {
        if ((from == null) != (to == null)) {
            throw new BadRequestException("from and to must be provided together");
        }
        Page<Event> events = eventService.getEvents(
                projectId,
                userId,
                type == null ? eventType : type,
                from,
                to,
                source,
                sessionId,
                metadataFilters(params),
                pageable
        );
        return events.map(eventMapper::toDto);
    }

    @Operation(summary = "Ingest one event using X-API-Key")
    @PostMapping
    public ResponseEntity<IngestResultDto> createEvent(
            @RequestHeader(name = "X-API-Key", required = false) String apiKey,
            @RequestBody @Valid EventRequestDto dto
    ) {
        ApiKey authenticatedKey = apiKeyService.authenticate(apiKey);
        EventService.IngestOutcome outcome = eventService.ingest(
                authenticatedKey.getProject(),
                eventMapper.toEntity(dto)
        );
        log.info("single_event_ingested projectId={} eventId={} duplicated={}",
                authenticatedKey.getProject().getId(), dto.getEventId(), outcome.duplicated());
        HttpStatus status = outcome.duplicated() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status)
                .body(new IngestResultDto(eventMapper.toDto(outcome.event()), outcome.duplicated()));
    }

    @Operation(summary = "Ingest a partially valid batch of up to 500 events")
    @PostMapping("/batch")
    public BatchIngestResponseDto createBatch(
            @RequestHeader(name = "X-API-Key", required = false) String apiKey,
            @RequestBody List<EventRequestDto> events
    ) {
        ApiKey authenticatedKey = apiKeyService.authenticate(apiKey);
        if (events == null) {
            throw new BadRequestException("Request body must be an array");
        }
        if (events.size() > MAX_BATCH_SIZE) {
            throw new BadRequestException("Batch size must be <= " + MAX_BATCH_SIZE);
        }

        int accepted = 0;
        int duplicated = 0;
        List<BatchItemErrorDto> errors = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            EventRequestDto item = events.get(i);
            if (item == null) {
                errors.add(new BatchItemErrorDto(i, "event must not be null"));
                continue;
            }
            Set<ConstraintViolation<EventRequestDto>> violations = validator.validate(item);
            if (!violations.isEmpty()) {
                errors.add(new BatchItemErrorDto(i, firstViolation(violations)));
                continue;
            }
            try {
                EventService.IngestOutcome outcome = eventService.ingest(
                        authenticatedKey.getProject(),
                        eventMapper.toEntity(item)
                );
                if (outcome.duplicated()) {
                    duplicated++;
                } else {
                    accepted++;
                }
            } catch (RuntimeException ex) {
                errors.add(new BatchItemErrorDto(i, ex.getMessage()));
            }
        }
        log.info("batch_ingestion_finished projectId={} total={} accepted={} duplicated={} rejected={}",
                authenticatedKey.getProject().getId(), events.size(), accepted, duplicated, errors.size());
        return eventService.recordBatch(events.size(), accepted, duplicated, errors);
    }

    @Operation(summary = "Update event metadata for demo administration")
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @RequestBody @Valid EventRequestDto dto
    ) {
        Event event = eventMapper.toEntity(dto);
        Event updateEvent = eventService.updateEvent(id, event);
        return ResponseEntity.ok(eventMapper.toDto(updateEvent));
    }

    @Operation(summary = "Delete event for demo administration")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, String> metadataFilters(MultiValueMap<String, String> params) {
        Map<String, String> filters = new LinkedHashMap<>();
        params.forEach((key, values) -> {
            if (key.startsWith("metadata.")) {
                if (values.size() != 1) {
                    throw new BadRequestException("Metadata filter must have exactly one value: " + key);
                }
                filters.put(key.substring("metadata.".length()), values.get(0));
            }
        });
        return filters;
    }

    private String firstViolation(Set<ConstraintViolation<EventRequestDto>> violations) {
        ConstraintViolation<EventRequestDto> violation = violations.iterator().next();
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
