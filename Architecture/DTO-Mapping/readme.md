<h1 align="center">DTO и маппинг</h1>

<h2 align="center">Назначение DTO</h2>

DTO отделяют внешний контракт API от внутренней JPA-модели и фиксируют формат запросов/ответов.

<h2 align="center">Основные DTO</h2>

- `ProjectRequestDto`, `ProjectResponseDto` — проекты.
- `ApiKeyCreateRequestDto`, `ApiKeyCreatedResponseDto`, `ApiKeyResponseDto` — API-ключи.
- `EventRequestDto`, `EventResponseDto` — события с JSONB metadata.
- `BatchIngestResponseDto`, `BatchItemErrorDto` — результат пакетного приёма событий.
- DTO аналитики — ответы для DAU/WAU/MAU, retention, cohorts, funnels, sessions и top lists.

<h2 align="center">Маппер событий</h2>

`EventMapper` преобразует `EventRequestDto` в `Event` и `Event` в `EventResponseDto`.

Внешний контракт использует поле `type`, а доменная модель хранит его как `eventType`.
