# DTO и маппинг

## DTO

DTO отделяют внешний контракт API от внутренней модели данных:

- `EventRequestDto` — входящий запрос на создание/обновление события.
- `EventResponseDto` — исходящий ответ для событий.
- `DauResponseDto` — ответ для DAU.
- `EventTypeCount` — элемент ответа для аналитики по типам.

Валидация входящих данных задаётся через `@NotBlank`.

## Маппер

`EventMapper` конвертирует `EventRequestDto` в `Event` и `Event` в `EventResponseDto`.

Обновление пользователя в `PUT /api/events/{id}` не применяется: сервис сохраняет `userId` существующей сущности.
