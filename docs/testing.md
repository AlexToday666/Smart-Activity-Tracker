# Testing

Run unit tests:

```bash
mvn test
```

The suite contains Mockito unit tests and Testcontainers integration tests for PostgreSQL/Flyway-backed flows. Integration tests are annotated with `disabledWithoutDocker = true`, so environments with incompatible Docker clients still run unit coverage and compile the integration tests.

Covered areas include service validation, event CRUD compatibility, API-key-authenticated ingestion, analytics endpoints and Flyway schema bootstrapping.

ELK is not started in automated tests because it is heavy for a small backend project. Logging is covered by configuration review and runtime smoke checks documented in `docs/logging.md`.
