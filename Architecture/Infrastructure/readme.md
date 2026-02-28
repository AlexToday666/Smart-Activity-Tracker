<h1 align="center">Инфраструктура и запуск</h1>

<h2 align="center">Dockerfile</h2>

Сборка выполняется в два шага:

1. `maven:3.9.6-eclipse-temurin-17` — сборка `jar`.
2. `eclipse-temurin:17-jre` — минимальный runtime.

<h2 align="center">Docker Compose</h2>

Локальный стек поднимается командой:

```bash
docker compose -f ops/docker-compose.yml up -d --build
```

Сервисы:

- `app` — Spring Boot, порт `8080`.
- `db` — PostgreSQL 16, порт `5433:5432`.
- `prometheus` — порт `9090`.
- `grafana` — порт `3000`.
- `elasticsearch` — порт `9200`.
- `logstash` — порты `5000` и `9600`.
- `kibana` — порт `5601`.

<h2 align="center">Хранилища</h2>

Используются Docker volumes:

- `pgdata`
- `grafana_data`
- `esdata`
