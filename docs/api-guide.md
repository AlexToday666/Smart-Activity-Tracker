# API Guide

Base path: `/api/v1`. Legacy aliases under `/api` are kept for the demo UI and older examples.

## Projects

```bash
curl -sS -X POST http://localhost:8080/api/v1/projects \
  -H 'Content-Type: application/json' \
  -d '{"name":"Marketing Website","slug":"marketing-website","description":"Web funnel events"}'
```

## API Keys

```bash
curl -sS -X POST http://localhost:8080/api/v1/projects/1/api-keys \
  -H 'Content-Type: application/json' \
  -d '{"name":"local-ingest"}'
```

The `secret` field is returned only in this response. Later list calls never expose it.

## Single Ingestion

```bash
curl -sS -X POST http://localhost:8080/api/v1/events \
  -H 'Content-Type: application/json' \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "eventId":"evt-001",
    "userId":"user-42",
    "type":"purchase",
    "occurredAt":"2026-02-10T12:00:00Z",
    "source":"web",
    "sessionId":"sess-42",
    "metadata":{"country":"DE","device":"mobile","amount":99}
  }'
```

## Batch Ingestion

```bash
curl -sS -X POST http://localhost:8080/api/v1/events/batch \
  -H 'Content-Type: application/json' \
  -H "X-API-Key: $API_KEY" \
  -d '[{"eventId":"evt-002","userId":"u1","type":"landing_view"}]'
```

Response:

```json
{"total":1,"accepted":1,"duplicated":0,"rejected":0,"errors":[]}
```

## Event Search

```bash
curl -sS "http://localhost:8080/api/v1/events?projectId=1&type=purchase&metadata.country=DE&metadata.device=mobile&size=20"
```

Supported filters: `projectId`, `userId`, `type`, `eventType`, `from`, `to`, `source`, `sessionId`, `metadata.<key>`.

## Analytics

```bash
curl -sS "http://localhost:8080/api/v1/analytics/dau?projectId=1&from=2026-02-01T00:00:00Z&to=2026-03-01T00:00:00Z"
curl -sS "http://localhost:8080/api/v1/analytics/funnels?projectId=1&from=2026-02-01T00:00:00Z&to=2026-03-01T00:00:00Z&steps=landing_view,signup,purchase"
curl -sS "http://localhost:8080/api/v1/analytics/top-users?projectId=1&from=2026-02-01T00:00:00Z&to=2026-03-01T00:00:00Z"
```
