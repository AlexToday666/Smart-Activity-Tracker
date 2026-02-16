package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.dto.ActiveUsersResponseDto;
import com.example.ActivityTracker.dto.CohortResponseDto;
import com.example.ActivityTracker.dto.DauResponseDto;
import com.example.ActivityTracker.dto.FunnelResponseDto;
import com.example.ActivityTracker.dto.RetentionResponseDto;
import com.example.ActivityTracker.dto.SessionAnalyticsResponseDto;
import com.example.ActivityTracker.dto.TopUsersResponseDto;
import com.example.ActivityTracker.service.AnalyticsService;
import com.example.ActivityTracker.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/analytics", "/api/v1/analytics"})
public class AnalyticsController {
    private final EventService eventService;
    private final AnalyticsService analyticsService;

    public AnalyticsController(EventService eventService, AnalyticsService analyticsService) {
        this.eventService = eventService;
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "DAU by day buckets")
    @GetMapping("/dau")
    public Object getDau(
            @RequestParam(required = false) Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        if (projectId == null) {
            long count = eventService.countDistinctUsersBetween(from, to);
            return new DauResponseDto(from, to, count);
        }
        return analyticsService.activeUsers(projectId, from, to, "day");
    }

    @Operation(summary = "WAU by week buckets")
    @GetMapping("/wau")
    public ActiveUsersResponseDto getWau(
            @RequestParam Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return analyticsService.activeUsers(projectId, from, to, "week");
    }

    @Operation(summary = "MAU by month buckets")
    @GetMapping("/mau")
    public ActiveUsersResponseDto getMau(
            @RequestParam Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return analyticsService.activeUsers(projectId, from, to, "month");
    }

    @Operation(summary = "Retention by days after first event")
    @GetMapping("/retention")
    public RetentionResponseDto retention(
            @RequestParam Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "7") int days
    ) {
        return analyticsService.retention(projectId, from, to, days);
    }

    @Operation(summary = "Cohorts grouped by first event date")
    @GetMapping("/cohorts")
    public CohortResponseDto cohorts(
            @RequestParam Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return analyticsService.cohorts(projectId, from, to);
    }

    @Operation(summary = "Funnel conversion for ordered event types")
    @GetMapping("/funnels")
    public FunnelResponseDto funnel(
            @RequestParam Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam String steps
    ) {
        List<String> parsedSteps = Arrays.stream(steps.split(","))
                .map(String::trim)
                .filter(step -> !step.isBlank())
                .toList();
        return analyticsService.funnel(projectId, from, to, parsedSteps);
    }

    @Operation(summary = "Session analytics using sessionId or inactivity window")
    @GetMapping("/sessions")
    public SessionAnalyticsResponseDto sessions(
            @RequestParam Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return analyticsService.sessions(projectId, from, to);
    }

    @Operation(summary = "Top users by event count")
    @GetMapping("/top-users")
    public TopUsersResponseDto topUsers(
            @RequestParam Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return analyticsService.topUsers(projectId, from, to, limit);
    }

    @Operation(summary = "Top event types by count")
    @GetMapping({"/top-event-types", "/event-types"})
    public Object topEventTypes(
            @RequestParam(required = false) Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        if (projectId == null) {
            return eventService.countByEventTypeBetween(from, to);
        }
        return analyticsService.topEventTypes(projectId, from, to);
    }
}
