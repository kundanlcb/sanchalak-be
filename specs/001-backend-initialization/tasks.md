# Tasks: Spec 001 - Backend Initialization (Dashboard)

## Phase 1: Project & Dependencies (P0)
- [ ] **T001**: Update `build.gradle` (Spring Web, Security, JPA, Postgres, Flyway, JJWT, Lombok, ModelMapper, Validation, Testcontainers).
- [ ] **T002**: Configure `application.yml`:
    - Define `spring.datasource` (Postgres/H2).
    - Define `app.jwt.secret` (Base64 random string) and `app.jwt.expiration`.
    - Define `server.port: 8080`.
    - Define `logging.level.org.springframework.security: DEBUG`.

## Phase 2: Database & Entities (P0)
- [ ] **T003**: Create base package structure (`com.cm.sanchalak_be.config`, `controller`, `dto`, `entity`, `repository`, `service`).
- [ ] **T004**: Create Flyway migration `V1__init_schema.sql`:
    - Tables: `users`, `roles`, `classes`, `students`, `teachers`.
    - Constraints: `users.email` UNIQUE.
- [ ] **T005**: Create JPA Entities: `User` (extends `BaseEntity`), `Role` (Enum), `Class`, `Student`, `Teacher`.
- [ ] **T006**: Create Repositories (`UserRepository`, `StudentRepository`, `TeacherRepository`, `ClassRepository`).
- [ ] **T007**: Create Flyway migration `V2__seed_data.sql`:
    - Insert 1 Admin User (`admin@school.com` / `password`).
    - Insert 5 Dummy `classes`.
    - Insert 10 `students` (linked to classes).
    - Insert 2 `teachers`.

## Phase 3: Security & Authentication (P0)
- [ ] **T008**: Implement `JwtTokenProvider`: `generateToken(email)`, `validateToken(token)`, `getEmailFromToken(token)`.
- [ ] **T009**: Implement `JwtAuthenticationFilter`: Extends `OncePerRequestFilter`, extracts Bearer token, sets `SecurityContext`.
- [ ] **T010**: Implement `SecurityConfig` (`@EnableWebSecurity`):
    - `SecurityFilterChain`: Disable CSRF, Stateless Session, Permit `/api/auth/**`.
    - `corsConfigurationSource`: Allow `http://localhost:5173`.
    - `passwordEncoder`: `BCryptPasswordEncoder`.
- [ ] **T011**: Implement `AuthService`: `login(LoginRequest)` -> `AuthResponse`, `getCurrentUser()`.
- [ ] **T012**: Implement `AuthController`: `POST /api/auth/login`, `GET /api/auth/me`.

## Phase 4: Dashboard & Business Logic (P1)
- [ ] **T013**: Create DTO: `DashboardStatsDto` (totalStudents, totalTeachers, totalClasses, monthlyRevenue).
- [ ] **T014**: Implement `DashboardService`: Logic to aggregate `count()` from repositories.
- [ ] **T015**: Implement `DashboardController`: `GET /api/dashboard/stats` (Secured).

## Phase 5: Verification & Testing (P1)
- [ ] **T016**: Create `AuthIntegrationTest` (using `@SpringBootTest`, `MockMvc`, H2): Verify Login flow.
- [ ] **T017**: Create `DashboardIntegrationTest`: Verify stats endpoint (mock data/seed).
- [ ] **T018**: Manual Check: Start app (`./gradlew bootRun`), verify logs, verify schema creation.
