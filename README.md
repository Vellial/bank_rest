# Bank Cards API

REST API для управления банковскими картами.

## Требования

- Java 21
- PostgreSQL 17 (или Docker)

## Запуск через Docker Compose

```bash
docker-compose up -d
```

- **Приложение**: `http://localhost:8081`
- **PostgreSQL**: `localhost:5432`

### Запуск локально
Локально (без Docker)
Создайте пустую БД bankrest в PostgreSQL и запустите приложение через IDE или mvn spring-boot:run.

#### API документация
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs