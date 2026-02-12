# Tasks: Spec 001 - Backend Initialization (Dashboard)

## Phase 1: Project & Dependencies (P0)
- [x] **T001**: Update `build.gradle` (Spring Web, Security, JPA, Postgres, Flyway, JJWT 0.12.x, Lombok, ModelMapper, Validation, Testcontainers).
- [x] **T002**: Configure `application.yml`:
    - Define `spring.datasource` (Postgres/H2).
    - Define `app.jwt.secret` (Base64 random string) and `app.jwt.expiration`.
    - Define `server.port: 8080`.
    - Define `logging.level.org.springframework.security: DEBUG`.

## Phase 2: Database & Entities (P0)
- [x] **T003**: Create base package structure (`com.cm.sanchalak.config`, `controller`, `dto`, `entity`, `repository`, `service`).
- [x] **T004**: Create Flyway migration `V1__init_schema.sql`:
    - Tables: `users`, `classes`, `students`, `teachers`.
    - Constraints: `users.email` UNIQUE.
- [x] **T005**: Create JPA Entities: `User` (extends `BaseEntity`), `Role` (Enum), `Class`, `Student`, `Teacher`.
- [x] **T006**: Create Repositories (`UserRepository`, `StudentRepository`, `TeacherRepository`, `ClassRepository`).
- [x] **T007**: Create Flyway migration `V2__seed_data.sql`:
    - Insert 1 Admin User (`admin@school.com` / `password`).
    - Insert 5 Dummy `classes`.
    - Insert 10 `students` (linked to classes).
    - Insert 2 `teachers`.

## Phase 3: Security & Authentication (P0)
- [x] **T008**: Implement `JwtTokenProvider`: `generateToken(email)`, `validateToken(token)`, `getEmailFromToken(token)`.
- [x] **T009**: Implement `JwtAuthenticationFilter`: Extends `OncePerRequestFilter`, extracts Bearer token, sets `SecurityContext`.
- [x] **T010**: Implement `SecurityConfig` (`@EnableWebSecurity`):
    - `SecurityFilterChain`: Disable CSRF, Stateless Session, Permit `/api/auth/**`.
    - `corsConfigurationSource`: Allow `http://localhost:5173`.
    - `passwordEncoder`: `BCryptPasswordEncoder`.
- [x] **T011**: Implement `AuthService`: `login(LoginRequest)` -> `AuthResponse`, `getCurrentUser()`.
- [x] **T012**: Implement `AuthController`: `POST /api/auth/login`, `GET /api/auth/me`.

## Phase 4: Dashboard & Business Logic (P1)
- [x] **T013**: Create DTO: `DashboardStatsDto` (totalStudents, totalTeachers, totalClasses, monthlyRevenue).
- [x] **T014**: Implement `DashboardService`: Logic to aggregate `count()` from repositories.
- [x] **T015**: Implement `DashboardController`: `GET /api/dashboard/stats` (Secured).

## Phase 5: Verification & Testing (P1)
- [x] **T016**: Create `AuthIntegrationTest` (using `@SpringBootTest`, `MockMvc`, H2): Verify Login flow.
- [x] **T017**: Create `DashboardIntegrationTest`: Verify stats endpoint (mock data/seed).
- [x] **T018**: Manual Check: Start app (`./gradlew bootRun`), verify logs, verify schema creation.
