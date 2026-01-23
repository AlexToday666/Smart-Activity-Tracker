# Доступ к данным

## Ответственность

Слой доступа к данным реализован через Spring Data JPA и отвечает за получение/сохранение событий и аналитические выборки.

## Основной интерфейс

- `EventRepository` — наследуется от `JpaRepository<Event, Long>`.

## Запросы и выборки

Производные методы:

- `findByUserIdOrderByEventTimeDesc`
- `findByEventTypeOrderByEventTimeDesc`
- `findByEventTimeBetweenOrderByEventTimeDesc`
- `findByUserIdAndEventTimeBetweenOrderByEventTimeDesc`

Пользовательские JPQL запросы:

- `countByEventTypeBetween(from, to)` — агрегация по типам событий.
- `countDistinctUsersBetween(from, to)` — DAU по уникальным пользователям.

В аналитике используются границы `[from, to)`: `from` включительно, `to` исключительно.
Для списков `between` соответствует включающим границам по стандарту JPA.

## Производительность

Индексы определены на уровне миграции:

- `user_id`
- `event_type`
- `event_date`

Это ускоряет выборки по пользователю, типу и диапазону времени.
