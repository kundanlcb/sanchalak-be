# Quickstart: Backend Initialization

## Prerequisites
- Java 25 installed (`java -version`)
- Docker Desktop running (for Postgres)
- Gradle installed (or use `./gradlew`)

## Setup
1. **Configure Environment**:
   Ensure `src/main/resources/application.yml` is created (Task T002).
   For local dev without Docker, ensure `spring.profiles.active=dev` is set to use H2.

2. **Run Application**:
   ```bash
   ./gradlew bootRun
   ```

3. **Verify Health**:
   - URL: `http://localhost:8080/actuator/health` (if actuator enabled)
   - Or try Login: `POST http://localhost:8080/api/auth/login`

## Testing
- **Run Unit Tests**:
  ```bash
  ./gradlew test
  ```
- **Manual API Test**:
  Use `contracts/openapi.yaml` with Postman or Swagger UI.
