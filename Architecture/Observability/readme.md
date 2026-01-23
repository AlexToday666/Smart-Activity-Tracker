# Наблюдаемость

## Метрики

- Метрики экспортируются через Micrometer и доступны на `/actuator/prometheus`.
- В `application.yml` добавлен тег `application=smart-activity-tracker` для группировки метрик.

## Prometheus

`prometheus.yml` настраивает сбор метрик с `app:8080` и пути `/actuator/prometheus`.

## Grafana

В `docker-compose.yml` Grafana поднимается на порту `3000` с дефолтными учётными данными `admin/admin`.
