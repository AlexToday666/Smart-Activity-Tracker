<h1 align="center">Доступ к данным</h1>

<h2 align="center">Ответственность</h2>

Слой доступа к данным реализован через Spring Data JPA и отвечает за сохранение событий, проектов, API-ключей и аналитические выборки.

<h2 align="center">Репозитории</h2>

- `ProjectRepository` — проекты.
- `ApiKeyRepository` — поиск активных ключей по hash и список ключей проекта.
- `EventRepository` — события, фильтрация, проверки идемпотентности и аналитические запросы.

<h2 align="center">События</h2>

`events` хранит:

- `project_id`
- `event_id`
- `user_id`
- `event_type`
- `occurred_at`
- `received_at`
- `metadata` как `jsonb`
- `source`
- `session_id`

<h2 align="center">Индексы</h2>

Ключевые индексы создаются Flyway-миграцией:

- `unique(project_id, event_id)`
- `(project_id, occurred_at)`
- `(project_id, event_type)`
- `(project_id, user_id)`
- `GIN(metadata)`

<h2 align="center">Границы времени</h2>

Аналитика использует диапазон `[from, to)`: `from` включительно, `to` исключительно.
