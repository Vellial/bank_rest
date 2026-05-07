# Bank Cards API

REST API для управления банковскими картами.

## Требования

- Java 21
- PostgreSQL 17 (или Docker)

## Запуск через Docker Compose

```bash
docker-compose up -d
```

- **Приложение**: `http://localhost:8080`
- **PostgreSQL**: `localhost:5432`

### Запуск локально
Настройте 'application.yml':
```yaml
spring:
datasource:
url: jdbc:postgresql://localhost:5432/bankrest
username: postgres
password: postgres
```

#### API документация
• Swagger UI: http://localhost:8080/swagger-ui.html
• OpenAPI JSON: http://localhost:8080/v3/api-docs