# Implementation Plan: Attendance Service

**Branch**: `003-attendance-service` | **Date**: 2026-02-12 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-attendance-service/spec.md`

## Summary

Implement a comprehensive Attendance Service for Sanchalak to allow teachers to mark daily attendance and admins/students to track it.
This feature includes a bulk attendance API, individual tracking, audit trails (marked/modified by), and statistical summaries.
It aligns with the frontend `attendance-tracking` feature. We will use a new `attendance_records` table linked to `Student` and `Class`.

## Technical Context

**Language/Version**: Java 25 (Spring Boot 4.0.2)
**Primary Dependencies**: Spring Data JPA, Spring Security, Validation, JJWT 0.12.x, Flyway
**Storage**: PostgreSQL (Flyway Migration)
**Testing**: JUnit 5, Mockito, Testcontainers (Integration)
**Target Platform**: Web Backend (REST API)
**Project Type**: Single Spring Boot Application
**Performance Goals**: Bulk mark 50 students < 200ms
**Constraints**: Zero-trust security (Role-checks), Audit required

## Constitution Check

*GATE: Passed*

1.  **API-First**: Defined in `contracts/api.yaml`.
2.  **Layered Arch**: Controller -> Service -> Repo structure planned.
3.  **Security**: `@PreAuthorize` will be used.
4.  **DB Integrity**: Flyway V4 script planned. Constraints on (student_id, date).
5.  **Testing**: `AttendanceIntegrationTest` planned.
6.  **Scalability**: Standard stateless REST.

## Project Structure

### Documentation (this feature)

```text
specs/003-attendance-service/
├── plan.md              # This file
├── research.md          # Research on Holidays & Audit
├── data-model.md        # ER Diagram & Schema
├── quickstart.md        # Run instructions
├── contracts/           # OpenAPI Spec
│   └── api.yaml
└── tasks.md             # Implementation Tasks
```

### Source Code

```text
src/main/java/com/cm/sanchalak/
├── controller/
│   └── AttendanceController.java
├── service/
│   └── AttendanceService.java
├── repository/
│   └── AttendanceRepository.java
├── entity/
│   ├── AttendanceRecord.java
│   └── AttendanceStatus.java (Enum)
└── dto/
    ├── MarkAttendanceRequest.java
    ├── BulkMarkAttendanceRequest.java
    └── AttendanceSummaryDto.java

src/main/resources/db/migration/
└── V4__attendance_schema.sql
```

## Complexity Tracking

N/A
