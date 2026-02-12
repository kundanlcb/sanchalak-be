# Research: Attendance Service

**Feature**: 003-attendance-service
**Status**: Completed

## 1. Unknowns & Clarifications

### Q: How to handle "Holiday" status effectively?
- **Option A**: Use a simple Enum status `HOLIDAY`. This relies on the teacher marking everyone as "Holiday" for that day.
- **Option B**: Create a separate `AcademicCalendar` table to define holidays globally, blocking attendance marking on those days.
- **Decision**: **Option A (Enum)** for this MVP.
- **Rationale**: The spec defines `AttendanceStatus` including `Holiday`. Implementing a full `AcademicCalendar` is out of scope for "Attendance Service" spec (might be part of `004-school-ops`). The goal is to allow tracking status. Teachers can bulk-mark "Holiday" if needed, or simply not mark attendance. However, `sanchalan` (frontend) expects a status. We will stick to the Enum as requested in the Spec.

### Q: Audit Trail Implementation
- **Context**: Spec requires `markedBy`, `markedDate`, `modifiedBy`, `modifiedDate`.
- **Option A**: Manual setting in Service layer.
- **Option B**: Spring Data JPA `@EnableJpaAuditing`.
- **Decision**: **Option B**.
- **Rationale**: Standard Spring Boot constitution (Clean Code). We can use `Auditable` base class (or verify if `BaseEntity` already has it).
- **Check**: Let's check `BaseEntity`.

### Q: Bulk Operations Performance
- **Context**: Marking 50 students at once.
- **Approach**: `saveAll()` in JPA.
- **Constraint**: Must ensure no duplicates.
- **Decision**: Use `saveAll()` transactional method. Use `findByStudentAndDate` checks or unique constraints.
- **Best Practice**: `UniqueConstraint` on `student_id + date`.

## 2. Technology Choices

- **Persistence**: Spring Data JPA.
- **Validation**: `jakarta.validation`.
- **Security**: Method-level `@PreAuthorize` (Role: ADMIN, TEACHER).

## 3. Integration Patterns

- **Frontend Sync**: The frontend sends dates as `YYYY-MM-DD`. Backend must parse `LocalDate`.
- **Response Format**: Summary stats must be calculated or cached. For now, on-the-fly calculation via Repository aggregates (Group By) is sufficient for class sizes < 100.
