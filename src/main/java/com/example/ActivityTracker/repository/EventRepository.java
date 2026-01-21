package com.example.ActivityTracker.repository;

import com.example.ActivityTracker.dto.EventTypeCount;
import com.example.ActivityTracker.model.Event;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 1) Список событий пользователя (пагинация)
    Page<Event> findByUserIdOrderByEventTimeDesc(String userId, Pageable pageable);

    // 2) Список событий по типу (пагинация)
    Page<Event> findByEventTypeOrderByEventTimeDesc(String eventType, Pageable pageable);

    // 3) События за интервал времени (пагинация)
    Page<Event> findByEventTimeBetweenOrderByEventTimeDesc(Instant from, Instant to, Pageable pageable);

    // 4) События пользователя за интервал (пагинация)
    Page<Event> findByUserIdAndEventTimeBetweenOrderByEventTimeDesc(
            String userId, Instant from, Instant to, Pageable pageable
    );

    // 5) Быстрая проверка существования (удобно для валидаций/условий)
    boolean existsByUserId(String userId);

    // 6) Агрегация: сколько событий каждого типа за период
    @Query("""
           select new com.example.ActivityTracker.dto.EventTypeCount(e.eventType, count(e))
           from Event e
           where e.eventTime >= :from and e.eventTime < :to
           group by e.eventType
           order by count(e) desc
           """)
    List<EventTypeCount> countByEventTypeBetween(@Param("from") Instant from, @Param("to") Instant to);

    // 7) DAU-like: количество уникальных пользователей за период (простейший вариант)
    @Query("""
           select count(distinct e.userId)
           from Event e
           where e.eventTime >= :from and e.eventTime < :to
           """)
    long countDistinctUsersBetween(@Param("from") Instant from, @Param("to") Instant to);
}
