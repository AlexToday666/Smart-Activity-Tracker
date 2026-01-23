# Инфраструктура и запуск

## Dockerfile

Сборка выполняется в два шага:

1. `maven:3.9.6-eclipse-temurin-17` — сборка `jar`.
2. `eclipse-temurin:17-jre` — минимальный runtime.

## docker-compose

Состав окружения:

- `db` (PostgreSQL 16), порт `5433:5432`
- `app` (Spring Boot), порт `8080`
- `prometheus` (v2.52.0), порт `9090`
- `grafana` (10.4.2), порт `3000`

Переменные для приложения прокидываются через environment и соответствуют `application.yml`.

## Хранилища

Используются volume-тома:

- `pgdata` — данные PostgreSQL
- `grafana_data` — данные Grafana
