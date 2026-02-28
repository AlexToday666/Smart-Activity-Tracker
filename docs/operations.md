# Operations

Start everything:

```bash
docker compose -f ops/docker-compose.yml up -d --build
```

Local addresses:

- App: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- PostgreSQL: `localhost:5433`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (`admin` / `admin`)
- Elasticsearch: `http://localhost:9200`
- Logstash monitoring: `http://localhost:9600`
- Kibana: `http://localhost:5601`

Environment variables are documented in `.env.example`.

Troubleshooting:

- App cannot connect to DB: check `SPRING_DATASOURCE_*`.
- No metrics in Prometheus: check `ops/monitoring/prometheus.yml`.
- No logs in Kibana: check Logstash logs and Elasticsearch indices.
