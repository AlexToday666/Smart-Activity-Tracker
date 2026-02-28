# Observability

Prometheus endpoint: `/actuator/prometheus`.

Useful metrics:

- `events_ingested_total`
- `events_duplicate_total`
- `events_rejected_total`
- `events_batch_size`
- `events_batch_duration_seconds`
- `analytics_requests_total`
- `analytics_query_duration_seconds`
- `api_keys_usage_total`
- `authentication_failures_total`

Grafana dashboard JSON lives in `infra/grafana/dashboards/smart-activity-tracker.json`.

Look at metrics first for rate and latency changes. Use Kibana logs when the metric points to a specific failure mode such as validation failures, duplicates, auth failures or slow analytics requests.
