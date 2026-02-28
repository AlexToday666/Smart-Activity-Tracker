<h1 align="center">Тестирование</h1>

<h2 align="center">Unit-тесты</h2>

`EventServiceTest` использует Mockito для проверки сервисной логики без реальной БД.

<h2 align="center">Интеграционные тесты</h2>

- `EventControllerIntegrationTest`
- `AnalyticsControllerIntegrationTest`

Интеграционные тесты используют Spring Boot, MockMvc, PostgreSQL Testcontainers и Flyway migrations.

<h2 align="center">Запуск</h2>

```bash
mvn test
```

Если совместимый Docker недоступен, Testcontainers-тесты пропускаются через `disabledWithoutDocker = true`, а unit-тесты продолжают выполняться.
