# Smart Activity Tracker (backend)

<p align="center">
  <img src="./docs/assets/image.png" alt="Smart Activity Tracker logo" width="300" height="300">
</p>

Backend-сервис на Java 17 / Spring Boot для приёма пользовательских событий, хранения в PostgreSQL и расчёта простой аналитики (DAU и счётчики по типам событий).

## Возможности

- Приём событий с произвольными метаданными.
- CRUD и фильтрация событий (по пользователю, типу, диапазону времени).
- Пагинация и сортировка через стандартный `Pageable`.
- Базовая аналитика: DAU и топ типов событий за период.
- Наблюдаемость через Actuator + Prometheus/Grafana.
- Документация API через OpenAPI (springdoc).

## Стек

- Java 17, Spring Boot 3.2.3
- Spring Web, Validation, Data JPA
- PostgreSQL + Flyway
- Micrometer + Prometheus + Grafana
- springdoc-openapi
- Testcontainers + JUnit 5 + Mockito

## Архитектура (обзор)

```
Client
  │
  ▼
REST API (controllers)
  │ DTO/Mapper
  ▼
Service (business rules, validation)
  │
  ▼
Repository (JPA queries)
  │
  ▼
PostgreSQL (events)
```

Подробности по слоям вынесены в `Architecture/`:

- `Architecture/API/readme.md`
- `Architecture/Service/readme.md`
- `Architecture/Data-Access/readme.md`
- `Architecture/Domain-Model/readme.md`
- `Architecture/DTO-Mapping/readme.md`
- `Architecture/Database-Migrations/readme.md`
- `Architecture/Configuration/readme.md`
- `Architecture/Observability/readme.md`
- `Architecture/Infrastructure/readme.md`
- `Architecture/Testing/readme.md`

## Структура проекта

- `src/main/java/.../controller` — REST API
- `src/main/java/.../service` — бизнес-логика
- `src/main/java/.../repository` — JPA доступ к данным
- `src/main/java/.../model` — JPA сущности
- `src/main/java/.../dto` — DTO
- `src/main/java/.../mapper` — преобразования DTO <-> entity
- `src/main/java/.../exception` — обработка ошибок
- `src/main/resources/db/migration` — миграции Flyway
- `docker-compose.yml`, `Dockerfile` — инфраструктура запуска
- `prometheus.yml` — конфигурация Prometheus

## Модель данных

Таблица `events`:

| Поле | Тип | Описание |
| --- | --- | --- |
| `id` | BIGSERIAL | PK |
| `user_id` | TEXT | идентификатор пользователя |
| `event_type` | TEXT | тип события |
| `event_date` | TIMESTAMPTZ | время события |
| `metadata` | TEXT | метаданные (строка, обычно JSON) |
| `created_at` | TIMESTAMPTZ | время записи в БД |

`Event` в коде маппится на эти столбцы, а `eventTime` связан с `event_date`.

## API

### События

- `GET /api/events` — список событий (пагинация).
  - Фильтры: `userId`, `eventType`, `from`, `to`.
  - `from` и `to` должны передаваться вместе.
  - Пример пагинации: `?page=0&size=20&sort=eventTime,desc`.
- `GET /api/events/{id}` — получить событие по id.
- `POST /api/events` — создать событие.
- `PUT /api/events/{id}` — обновить событие (обновляются `eventType`, `metadata`, `eventTime`; `userId` не меняется).
- `DELETE /api/events/{id}` — удалить событие.

Пример создания события:

```bash
curl -sS -X POST http://localhost:8080/api/events \
  -H 'Content-Type: application/json' \
  -d '{"userId":"u1","eventType":"click","eventTime":"2026-01-21T12:00:00Z","metadata":"{\"button\":\"buy\"}"}'
```

### Аналитика

- `GET /api/analytics/event-types?from=...&to=...` — счётчики по типам событий.
- `GET /api/analytics/dau?from=...&to=...` — DAU за период.

В аналитике время считается как `[from, to)`: `from` включительно, `to` исключительно.

### Документация OpenAPI

Swagger UI доступен на стандартном пути springdoc (`/swagger-ui.html`, редирект на `/swagger-ui/index.html`), если настройки не переопределены.

## Валидация и ошибки

- `EventRequestDto` валидируется через `@Valid` и `@NotBlank`.
- Ошибки возвращаются в формате `ApiError`:

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "userId is required",
  "path": "/api/events",
  "timestamp": "2026-01-21T12:00:00Z"
}
```

Исключения `NotFoundException` и `BadRequestException` маппятся в 404/400. Остальные — в 500.

## Конфигурация

В `src/main/resources/application.yml` источники БД ожидаются через переменные окружения:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Запуск

### Docker Compose

```bash
docker compose up -d --build
```

Поднимаются сервисы:

- `app` — порт `8080`
- `db` — PostgreSQL (хост-порт `5433` -> контейнер `5432`)
- `prometheus` — порт `9090`
- `grafana` — порт `3000` (логин/пароль: `admin`/`admin`)

Остановить:

```bash
docker compose down -v
```

### Локально (без Docker)

Требования: JDK 17, Maven, внешняя PostgreSQL.

```bash
mvn spring-boot:run
```

## Наблюдаемость

Actuator включён, доступны эндпоинты:

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

Prometheus настраивается через `prometheus.yml`, Grafana доступна на `http://localhost:3000`.

## Миграции Flyway

Миграции лежат в `src/main/resources/db/migration` и применяются автоматически при старте приложения.
Формат имени: `V<версия>__<описание>.sql`.

## Тестирование

- Unit: `EventServiceTest` (Mockito).
- Integration: `EventControllerIntegrationTest`, `AnalyticsControllerIntegrationTest` (Testcontainers + Postgres).

Запуск:

```bash
mvn test
```

Для интеграционных тестов нужен Docker.
