<h1 align="center">API слой</h1>

<h2 align="center">Ответственность</h2>

API слой принимает HTTP-запросы, валидирует входные данные, преобразует DTO и передаёт управление в сервисный слой.

<h2 align="center">Основные контроллеры</h2>

- `ProjectController` — управление проектами и API-ключами (`/api/v1/projects`).
- `EventController` — приём одиночных и пакетных событий, поиск и demo CRUD событий (`/api/v1/events`).
- `AnalyticsController` — аналитические HTTP-методы (`/api/v1/analytics`).

<h2 align="center">Основные контракты</h2>

- `ProjectRequestDto`, `ProjectResponseDto` — проекты.
- `ApiKeyCreateRequestDto`, `ApiKeyCreatedResponseDto`, `ApiKeyResponseDto` — API-ключи.
- `EventRequestDto`, `EventResponseDto` — события.
- `BatchIngestResponseDto`, `BatchItemErrorDto` — отчёт пакетного приёма событий.
- DTO аналитики: DAU/WAU/MAU, retention, cohorts, funnels, sessions, top users и top event types.

<h2 align="center">Валидация и ошибки</h2>

- Валидация выполняется через `@Valid` и ограничения Bean Validation.
- Методы приёма событий требуют заголовок `X-API-Key`.
- Пакетный приём событий валидирует каждый элемент независимо и возвращает `errors[]` с индексом элемента.
- Ошибки сериализуются в единый формат `ApiError` через `GlobalExceptionHandler`.

<h2 align="center">Пагинация и сортировка</h2>

`GET /api/v1/events` принимает стандартные параметры `Pageable`:

- `page` — номер страницы, начиная с 0.
- `size` — размер страницы.
- `sort` — сортировка, например `occurredAt,desc`.

<h2 align="center">Формат времени</h2>

Все временные параметры передаются в ISO-8601, например `2026-02-10T12:00:00Z`.
