# Feature Specification: Student Management and Integrity Operations

**Feature Branch**: `007-student-integrity-ops`
**Created**: 2026-02-13
**Status**: Draft
**Input**: Implement Student CRUD (Edit/Delete) and fix data integrity gaps in Teacher/Subject deletion.

## User Scenarios & Testing

### User Story 1 - Student Data Management (Priority: P1)

As an Admin, I want to edit student details and archive (soft delete) students who leave the school, so that the student database remains accurate without losing historical data.

**Why this priority**: Essential for day-to-day operations. Currently, students cannot be updated or removed once created.
**Independent Test**: Update a student's mobile number via API and verify the change. Soft-delete a student and verify they are excluded from active lists but remain in the database.

**Acceptance Scenarios**:

1. **Given** a registered student, **When** the Admin updates their "Guardian Name" or "Mobile Number", **Then** the changes are saved and reflected in subsequent fetch requests.
2. **Given** a student with active fee records, **When** the Admin attempts to delete them, **Then** the system performs a "Soft Delete" (marks as inactive) instead of a hard delete, preserving financial history.
3. **Given** a student is soft-deleted, **When** fetching the "Active Students" list, **Then** the student does not appear.
4. **Given** a student is soft-deleted, **When** fetching the "Archived/All Students" list (with specific filter), **Then** the student appears with an "Inactive" status.

### User Story 2 - Operational Integrity (Priority: P2)

As an Admin, I want the system to prevent me from deleting Teachers or Subjects that are currently assigned to the Weekly Timetable (Routine), so that the schedule does not break.

**Why this priority**: Prevents critical data corruption. A missing teacher/subject in a routine causes the Timetable view to crash or show errors.
**Independent Test**: Assign a teacher to a class routine, then attempt to delete the teacher. The system should block the request with a specific error message.

**Acceptance Scenarios**:

1. **Given** a Teacher assigned to "Class 10 - Monday - Period 1", **When** I attempt to delete the Teacher, **Then** the system rejects the request with "Cannot delete teacher assigned to active class routine".
2. **Given** a Subject assigned to "Class 9 - Tuesday - Period 2", **When** I attempt to delete the Subject, **Then** the system checks `ClassRoutine` dependencies and rejects the request if found.
3. **Given** a Teacher with NO routine assignments, **When** I delete them, **Then** the system proceeds with the existing soft-delete logic.

### User Story 3 - Dashboard Data Accuracy (Priority: P3)

As an Admin, I want to see real "Teacher Performance" metrics on the dashboard based on actual student marks, so that I can identify top-performing classes.

**Why this priority**: Enhances the value of the dashboard (moving away from hardcoded mock data).
**Independent Test**: Add marks for a student in a specific subject. Verify the associated teacher's performance average updates on the dashboard.

**Acceptance Scenarios**:

1. **Given** exam marks are entered for "History" (taught by Teacher A), **When** loading the Dashboard "Teacher Performance" widget, **Then** Teacher A's score reflects the average of those marks.
2. **Given** no marks are entered for a term, **When** viewing the widget, **Then** it shows 0% or a neutral state, not random mock data.

---

## Functional Requirements

### Student Management API
- **FR-01**: `GET /api/students` enhanced with filtering (active/archived).
- **FR-02**: `PUT /api/students/{id}`: Endpoint to update student details (Name, Class, Guardian, Contact). Validation: Email must remain unique if changed.
- **FR-03**: `DELETE /api/students/{id}`: Soft-delete endpoint. Action: Set `deleted = true` (or `active = false`). Constraint: If data integrity requires, prevent hard delete if `StudentFeeMap` exists (which is already true for soft delete).

### Integrity Services
- **FR-04**: **TeacherService**: `deleteTeacher(id)` must check `classRoutineRepository.existsByTeacherId(id)`. Return `409 Conflict` or `400 Bad Request` with clear message on failure.
- **FR-05**: **AcademicService (Subject)**: `deleteSubject(id)` must check `classRoutineRepository.existsBySubjectId(id)`. Must also check `classSubjectRepository.existsBySubjectId(id)` (if not already covered).

### Dashboard Service
- **FR-06**: **getTeacherPerformance()**: Logic: Query `StudentMarks` joined with `ExamSchedule` -> `Subject` -> `ClassSubject` -> `Teacher`. Calculation: Average `marksObtained` / `maxMarks` * 100 per Teacher. Limit: Top 5 teachers or all teachers.

## Technical Constraints & Assumptions

- **Soft Delete Pattern**: Use a `deleted` flag (boolean) or `isActive` on `Student` entity. Filter all default queries by `deleted = false`.
- **Performance**: Dashboard queries involving joins (Marks -> Teacher) should be optimized or cached if dataset is large (for now, direct SQL/JPQL is fine).
- **Security**: Only `ADMIN` role can perform Deletes and Updates on structural data (Teachers, Subjects, Students).

## Success Criteria

- **SC-01**: Admin can successfully edit a student's name and soft-delete them without errors.
- **SC-02**: Attempting to delete a scheduled teacher returns a specific error message.
- **SC-03**: Dashboard reflects actual entered marks data, not loose mock values.

## Assumptions

- `Student` entity already has or can easily support a `deleted` flag.
- `StudentMarks` repository allows querying marks by Subject/Teacher relation (might need custom JPQL).
- Frontend handles the display of "Active" vs "Inactive" students based on the API response.
