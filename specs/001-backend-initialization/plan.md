# Implementation Plan - Spec 001: Backend Initialization (Dashboard)

## Technical Context

| Component | Status | Source/Target |
| :--- | :--- | :--- |
| **Project** | Initialize | `Spring Boot` 4.0.2, Java 25, `PostgreSQL` |
| **Dependencies** | Add | Spring Security, JJWT (jjwt-api 0.12.x), Lombok, ModelMapper, Flyway |
| **Database** | Create | Schema: `users`, `students`, `teachers`, `classes` |
| **API** | Implement | Auth Controller (`/api/auth`) & Dashboard Controller (`/api/dashboard`) |
| **Config** | Setup | `SecurityFilterChain` (CORS + Filter), `application.yml` |

## Constitution Check

| Principle | Check | Validation |
| :--- | :--- | :--- |
| **API-First** | ✅ | Controllers define strict DTOs (`LoginRequest`, `AuthResponse`) |
| **Layered Arch** | ✅ | Separating `AuthService` from `AuthController` |
| **Security** | ✅ | Using `SecurityFilterChain` with stateless JWT & BCrypt |
| **DB Integrity** | ✅ | Using Flyway for schema migrations |
| **Testing** | ✅ | JUnit 5 for Service & Controller integration tests |

## Phase 0: Research & Discovery

### Unknowns & Riskiest Assumptions
1.  **Frontend JWT Handling**: Confirm how the React app sends the token (Bearer header vs Cookie). *Assumption: Bearer Header*.
2.  **Database Connection**: Ensure Docker Compose setup works locally for Postgres.

### Research Tasks
- [ ] Verify `jjwt` library compatibility with Spring Boot 3 (signature algorithm HS256 vs RS256).

## Phase 1: Implementation Strategy

### Step 1: Project Scaffolding
1.  Update `build.gradle` (Dependencies: Security, Web, JPA, Postgres, Lombok, JJWT, Flyway).
2.  Create package structure: `com.cms.sanchalak.{config, controller, dto, entity, repository, service, exception}`.
3.  Configure `application.yml` (Datasource, JWT Secret, Server Port).

### Step 2: Database Schema (Flyway)
1.  Create `V1__init_schema.sql`:
    *   `users` table (id, email, password_hash, role, created_at).
    *   `students` table (id, name, admission_no, class_id).
    *   `teachers` table (id, name, subject).
    *   `classes` table (id, name, section).
2.  Seed initial data in `V2__seed_data.sql` (Admin user, dummy students/teachers).

### Step 3: Security Configuration
1.  Implement `JwtTokenProvider` (generate/validate token).
2.  Implement `JwtAuthenticationFilter` (extract token, load user context).
3.  Configure `SecurityConfig`:
    *   Enabled CORS (allow localhost:5173).
    *   Stateless session management.
    *   Permit `/api/auth/login`.
    *   Authenticate everything else.

### Step 4: Authentication API
1.  Create DTOs: `LoginRequest`, `AuthResponse`, `UserDto`.
2.  Implement `AuthService`:
    *   `login(email, password)`: Verify password hash, return JWT.
    *   `getCurrentUser()`: Extract from SecurityContext.
3.  Implement `AuthController`:
    *   `POST /api/auth/login`
    *   `GET /api/auth/me`

### Step 5: Dashboard API
1.  Create DTO: `DashboardStatsDto` (students, teachers, classes).
2.  Implement `DashboardService`:
    *   Aggregate counts via Repository methods (`count()`).
3.  Implement `DashboardController`:
    *   `GET /api/dashboard/stats`

## Phase 2: Verification

### User Scenarios
1.  **Start App**: Run `./gradlew bootRun` -> Success.
2.  **Login**: Postman `POST /api/auth/login` -> Returns Token.
3.  **Get Stats**: Postman `GET /api/dashboard/stats` (with Token) -> Returns correct JSON counts.

### Automated Tests
- [ ] `AuthServiceTest`: Verify password checking and token generation.
- [ ] `DashboardControllerTest`: Integration test strictly verifying JSON structure (mocking service).
