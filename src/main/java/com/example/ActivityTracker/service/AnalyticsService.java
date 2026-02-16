package com.example.ActivityTracker.service;

import com.example.ActivityTracker.dto.ActiveUsersPointDto;
import com.example.ActivityTracker.dto.ActiveUsersResponseDto;
import com.example.ActivityTracker.dto.CohortDto;
import com.example.ActivityTracker.dto.CohortResponseDto;
import com.example.ActivityTracker.dto.EventTypeCount;
import com.example.ActivityTracker.dto.FunnelResponseDto;
import com.example.ActivityTracker.dto.FunnelStepDto;
import com.example.ActivityTracker.dto.RetentionPointDto;
import com.example.ActivityTracker.dto.RetentionResponseDto;
import com.example.ActivityTracker.dto.SessionAnalyticsResponseDto;
import com.example.ActivityTracker.dto.TopEventTypesResponseDto;
import com.example.ActivityTracker.dto.TopUserDto;
import com.example.ActivityTracker.dto.TopUsersResponseDto;
import com.example.ActivityTracker.exception.BadRequestException;
import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.repository.EventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
    private static final Duration SESSION_INACTIVITY_WINDOW = Duration.ofMinutes(30);

    private final EventRepository eventRepository;
    private final EventService eventService;
    private final Counter requestsCounter;
    private final Timer queryTimer;

    public AnalyticsService(EventRepository eventRepository, EventService eventService, MeterRegistry meterRegistry) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.requestsCounter = Counter.builder("analytics.requests.total").register(meterRegistry);
        this.queryTimer = Timer.builder("analytics.query.duration").register(meterRegistry);
    }

    public ActiveUsersResponseDto activeUsers(Long projectId, Instant from, Instant to, String granularity) {
        return timed("active_users", projectId, () -> {
            eventService.validateTimeRange(from, to);
            ChronoUnit unit = switch (granularity) {
                case "day" -> ChronoUnit.DAYS;
                case "week" -> ChronoUnit.WEEKS;
                case "month" -> ChronoUnit.MONTHS;
                default -> throw new BadRequestException("Unsupported granularity: " + granularity);
            };
            Map<LocalDate, Set<String>> usersByBucket = new LinkedHashMap<>();
            for (Event event : eventRepository.findAnalyticsEvents(projectId, from, to)) {
                LocalDate bucket = bucket(event.getOccurredAt(), unit);
                usersByBucket.computeIfAbsent(bucket, ignored -> new HashSet<>()).add(event.getUserId());
            }
            List<ActiveUsersPointDto> points = usersByBucket.entrySet()
                    .stream()
                    .map(entry -> new ActiveUsersPointDto(entry.getKey(), entry.getValue().size()))
                    .toList();
            return new ActiveUsersResponseDto(projectId, from, to, granularity, points);
        });
    }

    public RetentionResponseDto retention(Long projectId, Instant from, Instant to, int days) {
        return timed("retention", projectId, () -> {
            eventService.validateTimeRange(from, to);
            if (days < 1 || days > 90) {
                throw new BadRequestException("days must be between 1 and 90");
            }
            Map<String, LocalDate> firstSeen = firstSeenByUser(projectId, from, to);
            Map<String, Set<LocalDate>> activityDates = activityDatesByUser(projectId, from, to);
            List<RetentionPointDto> points = new ArrayList<>();
            for (int day = 0; day <= days; day++) {
                long retained = 0;
                for (Map.Entry<String, LocalDate> entry : firstSeen.entrySet()) {
                    if (activityDates.getOrDefault(entry.getKey(), Set.of()).contains(entry.getValue().plusDays(day))) {
                        retained++;
                    }
                }
                long cohortUsers = firstSeen.size();
                points.add(new RetentionPointDto(day, cohortUsers, retained, rate(retained, cohortUsers)));
            }
            return new RetentionResponseDto(projectId, from, to, points);
        });
    }

    public CohortResponseDto cohorts(Long projectId, Instant from, Instant to) {
        return timed("cohorts", projectId, () -> {
            eventService.validateTimeRange(from, to);
            Map<LocalDate, Long> cohorts = firstSeenByUser(projectId, from, to).values()
                    .stream()
                    .collect(Collectors.groupingBy(date -> date, LinkedHashMap::new, Collectors.counting()));
            List<CohortDto> result = cohorts.entrySet()
                    .stream()
                    .map(entry -> new CohortDto(entry.getKey(), entry.getValue()))
                    .toList();
            return new CohortResponseDto(projectId, from, to, result);
        });
    }

    public FunnelResponseDto funnel(Long projectId, Instant from, Instant to, List<String> steps) {
        return timed("funnel", projectId, () -> {
            eventService.validateTimeRange(from, to);
            if (steps == null || steps.size() < 2) {
                throw new BadRequestException("At least two funnel steps are required");
            }
            Map<String, List<Event>> eventsByUser = eventsByUser(projectId, from, to);
            List<Long> usersByStep = new ArrayList<>();
            for (int targetStep = 0; targetStep < steps.size(); targetStep++) {
                long users = 0;
                for (List<Event> events : eventsByUser.values()) {
                    if (completedSteps(events, steps) > targetStep) {
                        users++;
                    }
                }
                usersByStep.add(users);
            }
            List<FunnelStepDto> results = new ArrayList<>();
            long start = usersByStep.get(0);
            for (int i = 0; i < steps.size(); i++) {
                long previous = i == 0 ? usersByStep.get(i) : usersByStep.get(i - 1);
                results.add(new FunnelStepDto(steps.get(i), usersByStep.get(i), rate(usersByStep.get(i), previous),
                        rate(usersByStep.get(i), start)));
            }
            return new FunnelResponseDto(projectId, from, to, steps, results);
        });
    }

    public SessionAnalyticsResponseDto sessions(Long projectId, Instant from, Instant to) {
        return timed("sessions", projectId, () -> {
            eventService.validateTimeRange(from, to);
            List<SessionWindow> sessions = buildSessions(projectId, from, to);
            long users = sessions.stream().map(SessionWindow::userId).distinct().count();
            double avgEvents = sessions.stream().mapToInt(SessionWindow::events).average().orElse(0);
            double avgDuration = sessions.stream().mapToLong(SessionWindow::durationSeconds).average().orElse(0);
            return new SessionAnalyticsResponseDto(projectId, from, to, sessions.size(), users, avgEvents,
                    avgDuration, "sessionId_or_30_minute_inactivity_window");
        });
    }

    public TopUsersResponseDto topUsers(Long projectId, Instant from, Instant to, int limit) {
        return timed("top_users", projectId, () -> {
            eventService.validateTimeRange(from, to);
            List<TopUserDto> users = eventRepository.topUsers(projectId, from, to)
                    .stream()
                    .limit(limit)
                    .map(row -> new TopUserDto((String) row[0], (Long) row[1]))
                    .toList();
            return new TopUsersResponseDto(projectId, from, to, users);
        });
    }

    public TopEventTypesResponseDto topEventTypes(Long projectId, Instant from, Instant to) {
        return timed("top_event_types", projectId, () -> {
            eventService.validateTimeRange(from, to);
            List<EventTypeCount> eventTypes = eventRepository.countByEventTypeBetween(projectId, from, to);
            return new TopEventTypesResponseDto(projectId, from, to, eventTypes);
        });
    }

    private Map<String, List<Event>> eventsByUser(Long projectId, Instant from, Instant to) {
        return eventRepository.findAnalyticsEvents(projectId, from, to)
                .stream()
                .collect(Collectors.groupingBy(Event::getUserId, LinkedHashMap::new, Collectors.toList()));
    }

    private Map<String, LocalDate> firstSeenByUser(Long projectId, Instant from, Instant to) {
        Map<String, LocalDate> firstSeen = new LinkedHashMap<>();
        for (Event event : eventRepository.findAnalyticsEvents(projectId, from, to)) {
            firstSeen.putIfAbsent(event.getUserId(), toDate(event.getOccurredAt()));
        }
        return firstSeen;
    }

    private Map<String, Set<LocalDate>> activityDatesByUser(Long projectId, Instant from, Instant to) {
        Map<String, Set<LocalDate>> dates = new HashMap<>();
        for (Event event : eventRepository.findAnalyticsEvents(projectId, from, to)) {
            dates.computeIfAbsent(event.getUserId(), ignored -> new HashSet<>()).add(toDate(event.getOccurredAt()));
        }
        return dates;
    }

    private int completedSteps(List<Event> events, List<String> steps) {
        int step = 0;
        for (Event event : events) {
            if (event.getEventType().equals(steps.get(step))) {
                step++;
                if (step == steps.size()) {
                    return step;
                }
            }
        }
        return step;
    }

    private List<SessionWindow> buildSessions(Long projectId, Instant from, Instant to) {
        List<SessionWindow> sessions = new ArrayList<>();
        Map<String, List<Event>> grouped = eventRepository.findAnalyticsEvents(projectId, from, to)
                .stream()
                .sorted(Comparator.comparing(Event::getUserId).thenComparing(Event::getOccurredAt))
                .collect(Collectors.groupingBy(this::sessionGroupingKey, LinkedHashMap::new, Collectors.toList()));
        for (List<Event> events : grouped.values()) {
            SessionWindow current = null;
            for (Event event : events) {
                if (current == null || startsNewSession(current, event)) {
                    current = new SessionWindow(event.getUserId(), event.getOccurredAt(), event.getOccurredAt(), 1);
                    sessions.add(current);
                } else {
                    current.include(event.getOccurredAt());
                }
            }
        }
        return sessions;
    }

    private boolean startsNewSession(SessionWindow current, Event event) {
        return Duration.between(current.lastEventAt(), event.getOccurredAt()).compareTo(SESSION_INACTIVITY_WINDOW) > 0;
    }

    private String sessionGroupingKey(Event event) {
        if (event.getSessionId() != null && !event.getSessionId().isBlank()) {
            return "session:" + event.getSessionId();
        }
        return "user:" + event.getUserId();
    }

    private LocalDate bucket(Instant instant, ChronoUnit unit) {
        LocalDate date = toDate(instant);
        if (unit == ChronoUnit.WEEKS) {
            return date.minusDays(date.getDayOfWeek().getValue() - 1L);
        }
        if (unit == ChronoUnit.MONTHS) {
            return date.withDayOfMonth(1);
        }
        return date;
    }

    private LocalDate toDate(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    private double rate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0;
        }
        return Math.round((numerator * 10000.0 / denominator)) / 100.0;
    }

    private <T> T timed(String queryName, Long projectId, java.util.function.Supplier<T> supplier) {
        requestsCounter.increment();
        log.info("analytics_query_started query={} projectId={}", queryName, projectId);
        return queryTimer.record(supplier);
    }

    private static final class SessionWindow {
        private final String userId;
        private final Instant startedAt;
        private Instant lastEventAt;
        private int events;

        private SessionWindow(String userId, Instant startedAt, Instant lastEventAt, int events) {
            this.userId = userId;
            this.startedAt = startedAt;
            this.lastEventAt = lastEventAt;
            this.events = events;
        }

        private void include(Instant occurredAt) {
            this.lastEventAt = occurredAt;
            this.events++;
        }

        private String userId() {
            return userId;
        }

        private Instant lastEventAt() {
            return lastEventAt;
        }

        private int events() {
            return events;
        }

        private long durationSeconds() {
            return Duration.between(startedAt, lastEventAt).toSeconds();
        }
    }
}
