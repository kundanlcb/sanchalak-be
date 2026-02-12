# Research: Backend Initialization Phase

## Unknowns & Decisions

### 1. Spring Boot 3 & JWT Compatibility
- **Question**: Which JWT library version supports Jakarta EE (Spring Boot 3)?
- **Research**: Spring Boot 3+ migrated from `javax.*` to `jakarta.*`. Older `io.jsonwebtoken:jjwt:0.9.1` relies on javax.
- **Decision**: Use `io.jsonwebtoken:jjwt-api:0.12.5` (and impl/jackson runtime deps).
- **Rationale**: Fully supports Jakarta namespace and Java 21 features.
- **Implication**: `JwtAuthenticationFilter` must import `jakarta.servlet.http.HttpServletRequest`.

### 2. Database Connection in Dev
- **Question**: Should we use H2 or Postgres for local dev?
- **Decision**: Postgres for "dev"/"prod" (Docker/Local), H2 for "test" (Memory).
- **Rationale**: Ensures dev environment matches prod (Postgres), while tests remain fast and isolated (H2).

### 3. Frontend Integration
- **Question**: How does the frontend send tokens?
- **Decision**: `Authorization: Bearer <token>` header.
- **Rationale**: Standard OIDC/OAuth2 pattern, easier to secure than cookies for mobile apps.

## Best Practices Adopted
- **Password Encoding**: Use `BCryptPasswordEncoder` (strength 10).
- **CORS**: Allow `http://localhost:5173` specifically, deny all others.
- **Testing**: `Testcontainers` for integration tests to verify Postgres compatibility, even if using H2 for local dev.
