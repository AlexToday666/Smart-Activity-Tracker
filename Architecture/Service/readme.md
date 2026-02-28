<h1 align="center">Сервисный слой</h1>

<h2 align="center">Ответственность</h2>

Сервисный слой инкапсулирует бизнес-логику, транзакции, проверки входных параметров, идемпотентность и аналитику.

<h2 align="center">Основные сервисы</h2>

- `ProjectService` — CRUD проектов.
- `ApiKeyService` — генерация, hash-значения, проверка и отзыв API-ключей.
- `EventService` — приём событий, идемпотентность, фильтрация и базовые операции с событиями.
- `AnalyticsService` — DAU/WAU/MAU, retention, cohorts, funnels, sessions, top users и top event types.

<h2 align="center">Транзакционность</h2>

Сервисы используют `@Transactional`; сценарии только для чтения помечены `@Transactional(readOnly = true)`.

<h2 align="center">Ошибки</h2>

- `NotFoundException` — ресурс не найден.
- `BadRequestException` — некорректный запрос или диапазон времени.
- `UnauthorizedException` — отсутствующий или невалидный `X-API-Key`.

<h2 align="center">Метрики</h2>

Сервисный слой инкрементирует бизнес-метрики приёма событий, дублей, отклонённых событий, использования API-ключей и latency аналитики.
