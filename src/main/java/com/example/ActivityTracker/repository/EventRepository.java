package com.example.ActivityTracker.repository;

import com.example.ActivityTracker.dto.EventTypeCount;
import com.example.ActivityTracker.model.Event;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    // 1) Список событий пользователя (пагинация)
    Page<Event> findByUserIdOrderByOccurredAtDesc(String userId, Pageable pageable);

    // 2) Список событий по типу (пагинация)
    Page<Event> findByEventTypeOrderByOccurredAtDesc(String eventType, Pageable pageable);

    // 3) События за интервал времени (пагинация)
    Page<Event> findByOccurredAtBetweenOrderByOccurredAtDesc(Instant from, Instant to, Pageable pageable);

    // 4) События пользователя за интервал (пагинация)
    Page<Event> findByUserIdAndOccurredAtBetweenOrderByOccurredAtDesc(
            String userId, Instant from, Instant to, Pageable pageable
    );

    // 5) Быстрая проверка существования (удобно для валидаций/условий)
    boolean existsByUserId(String userId);

    Optional<Event> findByProjectIdAndEventId(Long projectId, String eventId);

    boolean existsByProjectIdAndEventId(Long projectId, String eventId);

    // 6) Агрегация: сколько событий каждого типа за период
    @Query("""
           select new com.example.ActivityTracker.dto.EventTypeCount(e.eventType, count(e))
           from Event e
           where e.project.id = :projectId and e.occurredAt >= :from and e.occurredAt < :to
           group by e.eventType
           order by count(e) desc
           """)
    List<EventTypeCount> countByEventTypeBetween(
            @Param("projectId") Long projectId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
           select new com.example.ActivityTracker.dto.EventTypeCount(e.eventType, count(e))
           from Event e
           where e.occurredAt >= :from and e.occurredAt < :to
           group by e.eventType
           order by count(e) desc
           """)
    List<EventTypeCount> countByEventTypeBetween(@Param("from") Instant from, @Param("to") Instant to);

    // 7) DAU-like: количество уникальных пользователей за период (простейший вариант)
    @Query("""
           select count(distinct e.userId)
           from Event e
           where e.project.id = :projectId and e.occurredAt >= :from and e.occurredAt < :to
           """)
    long countDistinctUsersBetween(
            @Param("projectId") Long projectId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
           select count(distinct e.userId)
           from Event e
           where e.occurredAt >= :from and e.occurredAt < :to
           """)
    long countDistinctUsersBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
           select date_trunc(:bucket, e.occurredAt) as bucket, count(distinct e.userId) as users
           from Event e
           where e.project.id = :projectId and e.occurredAt >= :from and e.occurredAt < :to
           group by date_trunc(:bucket, e.occurredAt)
           order by date_trunc(:bucket, e.occurredAt)
           """)
    List<Object[]> activeUsersByBucket(
            @Param("projectId") Long projectId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("bucket") String bucket
    );

    @Query("""
           select e.userId, count(e)
           from Event e
           where e.project.id = :projectId and e.occurredAt >= :from and e.occurredAt < :to
           group by e.userId
           order by count(e) desc
           """)
    List<Object[]> topUsers(@Param("projectId") Long projectId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
           select e
           from Event e
           where e.project.id = :projectId and e.occurredAt >= :from and e.occurredAt < :to
           order by e.userId asc, e.occurredAt asc
           """)
    List<Event> findAnalyticsEvents(
            @Param("projectId") Long projectId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
