<h1 align="center">Конфигурация</h1>

<h2 align="center">application.yml</h2>

Файл `src/main/resources/application.yml` описывает подключение к БД, настройки Jackson, Actuator и Logstash.

<h2 align="center">База данных</h2>

Значения берутся из переменных окружения:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

<h2 align="center">Логирование</h2>

Для отправки структурированных JSON-логов в Logstash используются:

- `LOGSTASH_HOST`
- `LOGSTASH_PORT`

<h2 align="center">Actuator</h2>

Открыты HTTP-методы Actuator:

- `health`
- `info`
- `prometheus`
- `metrics`

Также включены readiness/liveness probes.
