# Тестирование

## Unit-тесты

`EventServiceTest` использует Mockito для изоляции слоя репозитория.

## Интеграционные тесты

- `EventControllerIntegrationTest`
- `AnalyticsControllerIntegrationTest`

Тесты поднимают PostgreSQL через Testcontainers и используют `MockMvc` для HTTP-проверок.

## Запуск

```bash
mvn test
```

Для интеграционных тестов требуется Docker.
