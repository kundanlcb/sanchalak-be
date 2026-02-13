# Feature Specification: School Operations & Dashboard Enhancements

**Feature Branch**: `006-school-operations-dashboard`
**Reference**: Frontend `004-school-ops`, `006-full-crud-ops`
**Created**: 13 Feb 2026
**Status**: Draft

## Summary
Implement missing backend functionality to support the Frontend's "School Operations" and "Full CRUD" requirements. This includes enhanced Teacher profiles, Routine/Timetable management, detailed Dashboard analytics, and full CRUD capabilities for critical entities.

## Requirements

### 1. Teacher Management (Enhanced)
**Goal**: Support detailed Teacher profiles and assignment capabilities.
- **Entity**: Update `Teacher` entity.
  - Add: `qualification` (String), `subjectSpecialization` (List<Subject>), `email` (String, unique), `phone` (String).
  - Relationship: One-to-One with `User` (existing), Many-to-Many with `Subject` (Specialization).
- **CRUD**: Implement `TeacherController` with full Create, Read, Update, Delete (Soft Delete) operations.
- **Validation**: Prevent deletion if Teacher is assigned to active Classes or Routines.

### 2. Academic Routine (Timetable)
**Goal**: Digitize the weekly class schedule.
- **Entity**: Create `ClassRoutine`.
  - Fields: `dayOfWeek` (MONDAY-SATURDAY), `period` (1-8), `startTime` (Time), `endTime` (Time).
  - Relationships: `Class` (Many-to-One), `Subject` (Many-to-One), `Teacher` (Many-to-One).
- **Validation**: 
  - Prevent double-booking a Teacher for the same day/period.
  - Prevent double-booking a Class for the same day/period.
- **API**: `GET /api/academic/routine?classId={id}` (Matrix view), `POST /api/academic/routine`.

### 3. Dashboard Analytics (Expanded)
**Goal**: Power the new Dashboard widgets.
- **Endpoints**:
  - `GET /api/dashboard/stats/gender-distribution`: Return count/percentage of Male/Female students.
  - `GET /api/dashboard/stats/teacher-performance`: Return average marks of students in classes taught by each teacher.
  - `GET /api/dashboard/activity-feed`: Return recent system events (e.g., "Student X paid fees", "Marks uploaded for Class Y").
    - *Note*: Requires an `ActivityLog` entity or service to track key events.

### 4. Full CRUD Operations
**Goal**: Enable complete management of school data.
- **Finance**: Add `PUT` and `DELETE` endpoints for `FeeCategory` and `FeeStructure` in `FinanceConfigController`.
  - Validation: Block deletion if linked to Payments.
- **Academic**: Add `PUT` and `DELETE` for `Class` and `Subject` in `AcademicController`.
- **Student**: Add `PUT` and `DELETE` (Soft Delete/Archive) for `StudentController`.

## User Scenarios

### User Story 1: Managing Teachers
As an Admin, I want to add a Teacher with their subject specialization so that I can assign them to the correct classes.
- **Scenario**: Admin creates "Mr. Smith", assigns "Math" specialization. System creates User account and Teacher profile.

### User Story 2: Creating a Timetable
As an Admin, I want to assign "Math" to "Class 10 - Period 1" with "Mr. Smith".
- **Scenario**: Admin selects Class 10, Monday, Period 1. Selects Subject Math. System filters Teachers specializing in Math. Admin selects Mr. Smith. System saves.

### User Story 3: Dashboard Insights
As an Admin, I want to see the gender ratio of my students.
- **Scenario**: Dashboard loads. API returns `{ "male": 120, "female": 100 }`. Donut chart renders.

## API Design

### Dashboard
```http
GET /api/dashboard/stats/gender
GET /api/dashboard/stats/teacher-performance
GET /api/dashboard/activity
```

### Teacher
```http
GET /api/teachers
POST /api/teachers
PUT /api/teachers/{id}
DELETE /api/teachers/{id}
```

### Routine
```http
GET /api/academic/routine?classId={id}
POST /api/academic/routine
DELETE /api/academic/routine/{id}
```

## Assumptions
- "Activity Feed" will be implemented as a simple log table (`ActivityLog`) populated by Aspect-Oriented Programming (AOP) or event listeners on critical services.
