# Architecture

Smart Activity Tracker is a Spring Boot backend for project-scoped event ingestion, idempotent storage and product analytics.

```mermaid
flowchart TD
    Client[SDK / Backend Client] -->|X-API-Key| API[Spring REST API]
    UI[Embedded Demo Console] --> API
    API --> Auth[API Key Authentication]
    Auth --> Ingest[Ingestion Service]
    Ingest --> DB[(PostgreSQL + JSONB)]
    API --> Query[Event Query Service]
    API --> Analytics[Analytics Service]
    Query --> DB
    Analytics --> DB
    API --> Metrics[Micrometer / Actuator]
    Metrics --> Prometheus[Prometheus]
    Prometheus --> Grafana[Grafana]
    API --> Logs[Structured JSON Logs]
    Logs --> Logstash[Logstash]
    Logstash --> Elasticsearch[Elasticsearch]
    Elasticsearch --> Kibana[Kibana]
```

## Components

- Controllers expose `/api/v1/projects`, `/api/v1/events` and `/api/v1/analytics`.
- Services own validation, idempotency, API key lifecycle and analytics calculations.
- Repositories use Spring Data JPA and PostgreSQL-specific JSONB functions where needed.
- Flyway owns every schema change.
- Micrometer exports operational counters, summaries and timers.
- Logback emits structured JSON to console and Logstash TCP input.

## Batch Ingestion Flow

```mermaid
sequenceDiagram
    participant Client
    participant API as EventController
    participant Keys as ApiKeyService
    participant Ingest as EventService
    participant DB as PostgreSQL

    Client->>API: POST /api/v1/events/batch + X-API-Key
    API->>Keys: authenticate(raw key)
    Keys->>DB: find active key hash
    DB-->>Keys: ApiKey(project)
    loop each item
        API->>API: validate item
        API->>Ingest: ingest(project, event)
        Ingest->>DB: find projectId + eventId
        alt duplicate
            DB-->>Ingest: existing event
        else new event
            Ingest->>DB: insert event JSONB metadata
        end
    end
    API-->>Client: total/accepted/duplicated/rejected/errors[]
```

## Logging Flow

```mermaid
flowchart LR
    App[Smart Activity Tracker] -->|JSON TCP 5000| Logstash
    Logstash --> Elasticsearch
    Elasticsearch --> Kibana
```
