# PolyPadel Backend (Spring Boot 3, Java 17)

Implementation bootstrap for the PolyPadel backend.

## Requirements
- Java 17+
- Maven 3.9+
- PostgreSQL (local) or use Docker Compose

Alternatively, use Docker to build/run without installing Java 17 locally.

## Run (Docker Compose)

```bash
# build the app image and start Postgres + app
docker compose up -d --build

# app runs on http://localhost:8080
```

## Run locally

```bash
# ensure JAVA_HOME is a JDK 17+ and on PATH
mvn -DskipTests package
java -jar target/polypadel-backend-0.0.1-SNAPSHOT.jar
```

Configure environment variables or set `src/main/resources/application.yml`:
- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- JWT_SECRET (base64 256-bit)
- JWT_EXP_HOURS
- CORS_ALLOWED_ORIGINS

## Testing

This project uses Testcontainers for integration tests (PostgreSQL).

```bash
mvn test
```

Integration tests require Docker (they auto-skip locally if Docker is unavailable).

## API Docs

When the app is running, OpenAPI docs and Swagger UI are available at:

- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Swagger UI: http://localhost:8080/swagger-ui/index.html

## Auth
- POST /auth/login { email, password } → returns `{ token, user }` and sets HttpOnly cookie `JWT`

## Notes
- JWT secret must be base64-encoded >= 256-bit secret.
- Cookie Secure flag should be enabled in production behind HTTPS.
