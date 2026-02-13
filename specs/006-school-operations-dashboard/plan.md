# Implementation Plan: School Operations & Dashboard

**Branch**: `006-school-operations-dashboard`
**Feature**: Enhanced Teachers, Routines, Dashboard, and Full CRUD
**Spec**: `specs/006-school-operations-dashboard/spec.md`

## Summary
Implement comprehensive school operations management including detailed Teacher profiles, Weekly Class Routines (Timetables), and enhanced Dashboard Analytics. Additionally, enable full CRUD (Update/Delete) capabilities for Core Academic and Finance entities to meet Frontend requirements.

## Technical Context
**Entities**:
- `Teacher`: Add `qualification`, `email`, `phone`, `specialization` (Many-to-Many with Subject).
- `ClassRoutine`: New Entity (Day, Period, Class, Subject, Teacher).
- `ActivityLog`: Simple entity for tracking critical system events (optional, or use simple mock servcie for feed).

**Security**: 
- `TEACHER` management is restricted to `ADMIN`.
- `ROUTINE` management is restricted to `ADMIN`.
- `DASHBOARD` stats viewable by `ADMIN`, `PRINCIPAL`.

**Validation**:
- **Consitency**: Cannot delete Classes/Subjects/FeeStructures/Teachers if they are in use (have dependent records).
- **Scheduling**: Prevent double-booking Teachers/Classes in `ClassRoutine`.

## Phases

### Phase 1: Enhanced Teacher Management
**Goal**: Detailed Teacher profiles and List management.
- **DTOs**: `TeacherRequest` (with subjects), `TeacherResponse`.
- **Entity**: Update `Teacher`.
- **Service**: Implement `create`, `update`, `delete` (safe delete), `getById`.
- **Controller**: `TeacherController` (Full CRUD).

### Phase 2: Academic Routine & Full CRUD
**Goal**: Timetables and managing Classes/Subjects.
- **Entity**: Create `ClassRoutine`.
- **Service**: 
  - `RoutineService`: Manage weekly slots, validate conflicts.
  - `AcademicService`: Add `updateClass`, `deleteClass` (safely), `updateSubject`, `deleteSubject`.
- **Controller**: `AcademicController` updates, new `RoutineController`.

### Phase 3: Finance CRUD
**Goal**: update/delete Fee config.
- **Service**: Update `FinanceService` to support modification of Structures/Categories.
- **Validation**: Ensure no active `StudentFeeMap` or `PaymentTransaction` exists before deletion.

### Phase 4: Dashboard Analytics
**Goal**: Power the new dashboard widgets.
- **Service**: `DashboardService` extensions.
  - `getGenderDistribution()`: Count by gender.
  - `getTeacherPerformance()`: Avg student marks per teacher's classes.
  - `getActivityFeed()`: Mocked or simple recent updates query.
- **Controller**: `DashboardController` new endpoints.

## Artifacts
```text
src/main/java/com/cm/sanchalak/
├── entity/ClassRoutine.java
├── controller/TeacherController.java
├── controller/RoutineController.java
└── service/RoutineService.java
```
