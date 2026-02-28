<h1 align="center">Доменная модель</h1>

<h2 align="center">Project</h2>

`Project` задаёт изоляцию данных. Все события и API-ключи принадлежат конкретному проекту.

Поля:

- `id`
- `name`
- `slug`
- `description`
- `createdAt`
- `updatedAt`

<h2 align="center">ApiKey</h2>

`ApiKey` хранит только hash секрета и используется для защиты HTTP-методов приёма событий.

Поля:

- `id`
- `project`
- `keyHash`
- `name`
- `active`
- `createdAt`
- `lastUsedAt`
- `revokedAt`

<h2 align="center">Event</h2>

`Event` — пользовательское событие, привязанное к проекту.

Поля:

- `id`
- `project`
- `eventId`
- `userId`
- `eventType`
- `occurredAt`
- `receivedAt`
- `metadata`
- `source`
- `sessionId`

<h2 align="center">Идемпотентность</h2>

Повторная отправка события с тем же `project_id` и `event_id` не создаёт дубль.
