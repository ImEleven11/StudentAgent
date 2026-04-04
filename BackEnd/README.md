# StudentAgent BackEnd

This module contains the Java backend for the AI career planning application.

## Architecture Rules

- Frontend and backend must remain physically separated.
- Controllers expose RESTful APIs only.
- Services hold business orchestration and security-sensitive logic.
- Persistence adapters isolate MySQL access behind mapper interfaces.
- Security is stateless and enforced with JWT.
- Public API responses follow the `code/message/data` contract from the architecture document.

## Current Scope

The initial backend foundation includes:

- Spring Boot project bootstrap
- unified API response envelope
- global exception handling
- JWT-based authentication and protected routes
- user registration, login, logout, profile query, and profile update
- student profile creation, resume upload, missing-field completion, and profile query/update
- chat session management, message history, assessment start, and guided missing-field completion dialogs
- job search/detail/path APIs and profile-job matching analyze/recommend/history APIs
- staged plan generation, adjustment, progress tracking, and task completion APIs
- report generation, detail query, polish, list, and export APIs
- assessment creation, submission, history query, and plan adjustment APIs
- learning and practice resource query APIs plus admin course upsert API
- market supply-demand, hot jobs, trend, and forecast APIs
- admin job import/profile refresh, usage stats, and system config APIs
- demo seed data for jobs, resources, market metrics, and base config
- Flyway migration for the user table
- Flyway migration for the student profile table
- Flyway migration for chat session and message tables
- Flyway migration for job and match tables
- Flyway migration for plan, task, and report tables
- Flyway migration for assessment tables
- Flyway migration for resource tables
- Flyway migration for market metric tables
- Flyway migration for system config and demo seed data
- Redis, Neo4j, and ML service integration placeholders in configuration

## Directory Layout

```text
BackEnd/
  src/main/java/com/uestc/studentagent/backend/
    api/
    common/
    config/
    health/
    security/
    user/
  src/main/resources/
    db/migration/
    mapper/
```

## Local Run

1. Configure a MySQL database and update the environment variables or `application.yml`.
2. From `BackEnd`, run `mvn spring-boot:run`.
3. Open Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

## Required Environment Variables

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `REDIS_HOST`
- `REDIS_PORT`
- `NEO4J_URI`
- `NEO4J_USERNAME`
- `NEO4J_PASSWORD`
- `LOCAL_STORAGE_ROOT`

## Security Notes

- Passwords are stored as BCrypt hashes.
- JWT is required for protected user APIs.
- CORS is restricted to configured origins.
- Sensitive configuration is externalized via environment variables.
