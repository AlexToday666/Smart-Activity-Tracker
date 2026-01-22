package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.dto.DauResponseDto;
import com.example.ActivityTracker.dto.EventTypeCount;
import com.example.ActivityTracker.service.EventService;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final EventService eventService;

    public AnalyticsController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(
            summary = "Получить счётчик событий по типам",
            description = "Возвращает список типов событий с количеством каждого типа за указанный период. " +
                    "Результаты отсортированы по убыванию количества."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Статистика успешно получена",
                    content = @Content(schema = @Schema(implementation = EventTypeCount.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный временной диапозон (from > to)"
            )
    })
    @GetMapping("/event-types")
    public List<EventTypeCount> getEventTypes(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return eventService.countByEventTypeBetween(from, to);
    }

    @Operation(
            summary = "Получить DAU(Daily Active Users)",
            description = "Возвращает количество уникальных пользователей за указанный период времени"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "DAU успешно рассчитан",
                    content = @Content(schema = @Schema(implementation = DauResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный временной диапозон (from > to)"
            )
    })
    @GetMapping("/dau")
    public DauResponseDto getDau(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        long count = eventService.countDistinctUsersBetween(from, to);
        return new DauResponseDto(from, to, count);
    }
}
