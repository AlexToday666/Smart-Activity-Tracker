# Доменная модель

## Сущность Event

`Event` — JPA сущность, отображённая на таблицу `events`.

### Поля и отображение

- `id` -> `id`
- `userId` -> `user_id`
- `eventType` -> `event_type`
- `eventTime` -> `event_date`
- `metadata` -> `metadata`
- `createdAt` -> `created_at`

### Особенности

- `@PrePersist` выставляет `createdAt` и `eventTime`, если они не заданы.
- `metadata` хранится как `TEXT` и обычно содержит JSON-строку.

### Индексы

Индексы заданы на уровне БД (см. миграции) для ускорения фильтрации.
