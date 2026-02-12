<!--
SYNC IMPACT REPORT
==================
Version: 1.0.0
Date: 2026-02-12
Type: Initial Ratification

Principles Established:
  I. API-First Design
  II. Layered Architecture (Clean Code)
  III. Security via Filter Chain (Zero Trust)
  IV. Database Integrity & Migrations
  V. Testing & Quality Assurance
  VI. Scalability & Performance

Rationale:
  - Establishes foundational rules for the Java Spring Boot backend.
  - Aligns with modern microservices/monolith best practices.
  - Prioritizes security (Owasp Top 10) and maintainability.
-->

# Sanchalak Backend Constitution

## Core Principles

### I. API-First Design & Contract Stability

**MUST** define clear RESTful API contracts (OpenAPI/Swagger) before writing implementation logic. All endpoints MUST use standard HTTP methods (GET, POST, PUT, DELETE) and return appropriate status codes. API responses MUST be strictly typed and versioned if breaking changes occur.

**Rationale**: The frontend relies on stable contracts. Backend changes without notice break client apps. API-First ensures both teams agree on the JSON structure before integration.

**Enforcement**:
- All controllers MUST be annotated with `@Operation` and `@ApiResponse`.
- DTOs (Data Transfer Objects) MUST be used for request/response bodies; NEVER expose Entity classes directly.
- Breaking changes require a new API version (e.g., `/api/v2/`).

### II. Layered Architecture (Clean Code)

**MUST** strictly adhere to the Controller -> Service -> Repository pattern.
- **Controllers**: Handle HTTP, validation, and DTO mapping. NO business logic.
- **Services**: Contain all business logic, transactions, and domain rules.
- **Repositories**: Handle database interactions only.

**Rationale**: Prevents "Spaghetti Code". Ensures separation of concerns, making the codebase testable and maintainable.

**Enforcement**:
- Controllers MUST NOT call Repositories directly.
- Business logic in Controllers is forbidden.
- Use dependency injection (`@RequiredArgsConstructor` or constructor injection) for all beans.

### III. Security via Filter Chain (Zero Trust)

**MUST** implement security at the filter chain level using Spring Security. All endpoints are "Deny All" by default unless explicitly permitted. Authentication MUST use Stateless JWT. Role-Based Access Control (RBAC) MUST be enforced at the method level using `@PreAuthorize`.

**Rationale**: Security cannot be an afterthought. Filters ensure that no request reaches the business logic without authentication.

**Enforcement**:
- `SecurityFilterChain` bean MUST define access rules.
- Passwords MUST be BCrypt hashed before storage.
- No session state in the server (Stateless).
- CORS MUST be configured to allow only trusted origins.

### IV. Database Integrity & Migrations

**MUST** use a schema migration tool (Flyway or Liquibase) for all database changes. Creating tables via Hibernate `ddl-auto` in production is PROHIBITED. All modification operations (Create, Update, Delete) MUST be wrapped in `@Transactional`.

**Rationale**: Database changes must be version-controlled and reproducible across environments (Dev -> Stage -> Prod). Transactions ensure data consistency.

**Enforcement**:
- `src/main/resources/db/migration` MUST contain SQL versioned scripts.
- `@Transactional` annotation on Service methods, not Controllers.
- Foreign keys and constraints MUST be defined in the database schema.

### V. Testing & Quality Assurance

**MUST** maintain high test coverage (>80% for Service layer).
- **Unit Tests**: JUnit 5 + Mockito for Services (fast, isolated).
- **Integration Tests**: `@SpringBootTest` with Testcontainers (H2/Postgres) for Controllers and Repositories.

**Rationale**: Refactoring without tests is dangerous. Integration tests verify that the SQL queries and JSON mapping work correctly.

**Enforcement**:
- Build pipeline (Gradle) MUST fail if tests fail.
- New features MUST include corresponding tests.
- Do not mock the database in Integration Tests; use an in-memory or containerized DB.

### VI. Scalability & Resilience

**MUST** code for statelessness to allow horizontal scaling. Long-running tasks MUST be asynchronous (`@Async` or Message Queue). External API calls MUST have timeouts and Circuit Breakers (Resilience4j).

**Rationale**: The system should handle load spikes without crashing. One failing external service shouldn't bring down the whole backend.

**Enforcement**:
- No state stored in instance variables of singleton beans.
- Use caching (`@Cacheable`) for read-heavy, low-change data.
- Global Exception Handler (`@ControllerAdvice`) MUST handle all runtime exceptions gracefully.

## Governance & Versioning

- **Constitution Version**: 1.0.0
- **Ratified**: 2026-02-12
- **Amendments**: Proposed via Pull Request, verified by Lead Architect.
- **Compliance**: Code reviews MUST check against these principles.

