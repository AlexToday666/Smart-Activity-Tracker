<h1 align="center">Наблюдаемость</h1>

<h2 align="center">Метрики</h2>

Метрики экспортируются через Micrometer и доступны на `/actuator/prometheus`.

Ключевые метрики:

- `events_ingested_total`
- `events_duplicate_total`
- `events_rejected_total`
- `events_batch_size`
- `events_batch_duration_seconds`
- `analytics_requests_total`
- `analytics_query_duration_seconds`
- `api_keys_usage_total`
- `authentication_failures_total`

<h2 align="center">Prometheus</h2>

`ops/monitoring/prometheus.yml` настраивает сбор метрик с `app:8080` по пути `/actuator/prometheus`.

<h2 align="center">Grafana</h2>

Grafana доступна на `http://localhost:3000`. Dashboard JSON лежит в `infra/grafana/dashboards/`.

<h2 align="center">Логи</h2>

Приложение пишет структурированные JSON-логи и отправляет их в Logstash, затем в Elasticsearch и Kibana.
