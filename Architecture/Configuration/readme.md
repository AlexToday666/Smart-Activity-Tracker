# Конфигурация

## application.yml

Файл `src/main/resources/application.yml` описывает подключения и параметры Actuator.

### База данных

Значения берутся из переменных окружения:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Actuator

Открыты эндпоинты:

- `health`
- `info`
- `prometheus`
- `metrics`

Дополнительно включены readiness/liveness пробы (`management.endpoint.health.probes.enabled=true`).
