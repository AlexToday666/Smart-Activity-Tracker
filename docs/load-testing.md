# Load Testing

Scripts live in `load-tests/` and use k6.

Batch ingest:

```bash
API_KEY=sat_xxx BASE_URL=http://localhost:8080 k6 run load-tests/batch-ingest.js
```

Analytics:

```bash
PROJECT_ID=1 BASE_URL=http://localhost:8080 k6 run load-tests/analytics.js
```

Watch these metrics while running tests:

- `events_batch_duration_seconds`
- `events_ingested_total`
- `events_duplicate_total`
- `analytics_query_duration_seconds`
- PostgreSQL CPU and disk I/O

Bottleneck-sensitive endpoints are batch ingestion, funnels, retention and sessions because they touch many events per request.
