package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.dto.DauResponseDto;
import com.example.ActivityTracker.dto.EventTypeCount;
import com.example.ActivityTracker.service.EventService;
import java.time.Instant;
import java.util.List;
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

    @GetMapping("/event-types")
    public List<EventTypeCount> getEventTypes(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return eventService.countByEventTypeBetween(from, to);
    }
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
