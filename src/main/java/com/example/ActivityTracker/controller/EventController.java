package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.dto.EventRequestDto;
import com.example.ActivityTracker.dto.EventResponseDto;
import com.example.ActivityTracker.exception.BadRequestException;
import com.example.ActivityTracker.mapper.EventMapper;
import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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


@Tag(name = "Events", description = "API для управления событиями пользователей")
@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventController(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @Operation(
            summary = "Получить событие по ID",
            description = "Возвращает событие с указанным идентификатором"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Событие найдено",
                    content = @Content(schema = @Schema(implementation = EventResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Событие не найдено"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(eventMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Получить список событий",
            description = "Возвращает список событий с возможностью фильтрации. " +
                    "Фильтрации можно комбинировать. Параметры from и to должны указываться вместе."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список событий успешно получен"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Некорректные параметры запроса (например, from указан без to)"
            )
    })
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

    @Operation(
            summary = "Создать новое событие",
            description = "Создаёт новое событие пользователя. Если eventTime не указан, " +
                    "автоматически устанавливается текущее время."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Событие успешно создано",
                    content = @Content(schema = @Schema(implementation = EventResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные (валидация не пройдена)"
            )
    })
    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody @Valid EventRequestDto dto) {
        Event event = eventMapper.toEntity(dto);
        Event savedEvent = eventService.createEvent(event);
        return ResponseEntity.ok(eventMapper.toDto(savedEvent));
    }

    @Operation(
            summary = "Обновить событие",
            description = "Обновляет существующее событие по ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Событие успешно обновлено",
                    content = @Content(schema = @Schema(implementation = EventResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Событие не найдено"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable Long id,
                                                        @RequestBody @Valid EventRequestDto dto) {
        Event event = eventMapper.toEntity(dto);
        Event updateEvent = eventService.updateEvent(id, event);
        return ResponseEntity.ok(eventMapper.toDto(updateEvent));
    }

    @Operation(
            summary = "Удалить событие",
            description = "Удаляет событие по идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Событие успешно удалено"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Событие не найдено"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
