# Smart Activity Tracker (backend)

Java 17 / Spring Boot 3.2 сервис для приёма событий, хранения в PostgreSQL и простой аналитики (DAU, счётчики по типам событий).

## Архитектура

- REST API: Spring MVC контроллеры `EventController`, `AnalyticsController`.
- Сервисный слой: `EventService` инкапсулирует бизнес-логику и работу с репозиторием.
- Доступ к данным: Spring Data JPA репозиторий для сущности `Event`.
- DTO/маппинг: `EventRequestDto`/`EventResponseDto` + `EventMapper` для изоляции модели БД от внешних контрактов.
- Миграции: Flyway (`src/main/resources/db/migration`, формат `V<версия>__<описание>.sql`).
- Хранение: PostgreSQL. Индексы по `user_id`, `event_type`, `event_date` для выборок и аналитики.

## Быстрый старт (Docker Compose)

Требования: Docker и Docker Compose.

```bash
docker compose up -d --build
```

Что поднимется:
- `app` — Spring Boot на порту `8080`
- `db` — PostgreSQL на хост-порту `5433` (прокинут в контейнер `5432`)

Проверки после запуска:
```bash
curl -sS http://localhost:8080/api/events
curl -sS http://localhost:8080/api/analytics/dau?from=2026-01-01T00:00:00Z&to=2026-01-02T00:00:00Z
```

Остановить и удалить всё с томом:
```bash
docker compose down -v
```

## Ручные проверки API (curl)

Создать событие:
```bash
curl -sS -X POST http://localhost:8080/api/events \
  -H 'Content-Type: application/json' \
  -d '{"userId":"u1","eventType":"click","timestamp":"2026-01-21T12:00:00Z"}'
```

Получить список событий (пагинация `page`, `size`):
```bash
curl -sS "http://localhost:8080/api/events?page=0&size=10"
```
Фильтры поддерживаются: `userId`, `eventType`, `from`, `to` (ISO‑8601, например `2026-01-21T00:00:00Z`).

Аналитика:
- DAU: `GET /api/analytics/dau?from=...&to=...`
- Счётчики по типам: `GET /api/analytics/event-types?from=...&to=...`

## Локальный запуск без Docker

Требования: JDK 17, Maven.
```bash
mvn spring-boot:run
```
Нужна внешняя PostgreSQL с параметрами:
- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/activity`
- `SPRING_DATASOURCE_USERNAME=activity`
- `SPRING_DATASOURCE_PASSWORD=activity`

## Миграции Flyway

- Файлы лежат в `src/main/resources/db/migration`.
- Формат имени: `V<версия>__<описание>.sql` (двойное подчёркивание перед описанием).
- Текущая миграция: `V1__create_events.sql` создаёт таблицу `events` и индексы.

## Полезное

- Логи приложения в Docker: `docker compose logs -f app`
- Если порт `5432` занят на хосте, используем `5433` (уже настроено в `docker-compose.yml`).
- При ошибках Flyway чаще всего проблема в формате имени файла или синтаксисе SQL.
