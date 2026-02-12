# Feature Specification: Backend Initialization & Dashboard APIs

**Feature Branch**: `001-backend-initialization`
**Created**: 2026-02-12
**Status**: Draft
**Input**: Initialize Spring Boot backend and implement core APIs to support the Sanchalak Web Dashboard (Authentication, User Roles, and Dashboard Statistics).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Secure Administrator Login (Priority: P0)

As an administrator, I want to log in securely using my credentials so that I can access the school management dashboard.

**Why this priority**: Fundamental entry point; no other feature works without authentication.

**Independent Test**: Send a POST request to `/api/auth/login` with valid credentials and verify a JWT token is returned.

**Acceptance Scenarios**:
1. **Given** a registered admin user "admin@school.com" with password "password", **When** I send a POST request to `/api/auth/login`, **Then** the system returns a 200 OK status and a JWT token in the response body.
2. **Given** invalid credentials, **When** I attempt login, **Then** the system returns 401 Unauthorized.
3. **Given** a valid JWT token, **When** I access a protected endpoint (e.g., `/api/users/me`), **Then** the system returns 200 OK with my user details.

### User Story 2 - Dashboard Statistics API (Priority: P1)

As an administrator, I want the dashboard to load key metrics quickly so that I can see the school's overview immediately upon login.

**Why this priority**: The dashboard is the landing page and requires aggregated data from multiple modules.

**Independent Test**: Seed the database with 5 students and 2 teachers, call `GET /api/dashboard/stats`, and verify the counts match.

**Acceptance Scenarios**:
1. **Given** 150 active students and 12 teachers in the database, **When** I request `GET /api/dashboard/stats`, **Then** the response JSON includes `{ "totalStudents": 150, "totalTeachers": 12 }`.
2. **Given** new fee payments recorded today, **When** I request the stats, **Then** the "monthlyRevenue" field reflects the sum of these payments.

### User Story 3 - Role-Based Access Control (Priority: P1)

As a system architect, I want to ensure API endpoints are protected by roles so that students cannot access administrative functions.

**Why this priority**: Security requirement to prevent unauthorized data access.

**Independent Test**: Attempt to access an Admin-only endpoint (e.g., `POST /api/teachers`) using a Student role token.

**Acceptance Scenarios**:
1. **Given** a user with "Student" role, **When** they request `GET /api/finance/stats`, **Then** the system returns 403 Forbidden.
2. **Given** a user with "Admin" role, **When** they request the same endpoint, **Then** the system returns 200 OK.

---

## Functional Requirements

### System Architecture
- **FR 1.1**: The backend must be initialized as a Spring Boot 3.x application using Java 17+.
- **FR 1.2**: The application must confirm to a layered architecture (Controller -> Service -> Repository).
- **FR 1.3**: The database must be configured for PostgreSQL (production) and H2 (development/test).

### Authentication & Security
- **FR 2.1**: Implement Spring Security with stateless JWT authentication.
- **FR 2.2**: detailed `User` entity with fields: `id` (UUID), `email` (unique), `password` (BCrypt encoded), `role` (Enum), `name`.
- **FR 2.3**: Endpoint `POST /api/auth/login` must validate credentials and return a signed JWT.
- **FR 2.4**: Endpoint `GET /api/auth/me` or `/api/users/me` must return the current authenticated user's profile.

### Dashboard Data
- **FR 3.1**: Endpoint `GET /api/dashboard/stats` must aggregate counts from the database.
- **FR 3.2**: Calculate `totalStudents`, `totalTeachers`, `totalClasses` from their respective tables.
- **FR 3.3**: Mock or calculate `monthlyRevenue` based on a basic `Payment` entity (if ready) or return 0 for initial phase.

### Core Entities (Minimal)
- **FR 4.1**: Define **Student** entity (id, name, classId, admissionNo).
- **FR 4.2**: Define **Teacher** entity (id, name, subjectSpecialization).
- **FR 4.3**: Define **Class** entity (id, name, section).

### API Compatibility
- **FR 5.1**: Ensure API paths match:
  - `/api/auth/login`
  - `/api/auth/me`
  - `/api/dashboard/stats`

---

## Technical Constraints & Assumptions

- **Mock Parity**: API responses must match the JSON structure expected by the frontend.
- **Database**: Use Docker Compose for local PostgreSQL or H2 in-memory if Docker is unavailable.
- **CORS**: Must be configured to allow requests from `http://localhost:5173` (Vite dev server).

---

## Success Criteria

1.  **Server Startup**: The Spring Boot application starts successfully on port 8080.
2.  **Auth Flow**: A React frontend running locally can authenticate against this backend and receive a token.
3.  **Data Consistency**: The `/api/dashboard/stats` endpoint returns accurate counts based on the seeded data.
