# Implementation Plan - Student Integrity & Operations

**Branch**: `007-student-integrity-ops`
**Feature**: Student Management, Integrity Checks & Dashboard Real Data
**Spec**: `specs/007-student-integrity-ops/spec.md`

## Technical Context

**Entities**:
- `Student`: Check for `deleted` or `isActive` field. If missing, needs strict migration/update.
- `Teacher` & `Subject`: Existing entities. No schema changes expected, only logic.
- `ClassRoutine`: Used for validation.

**Services**:
- `StudentService`: **NEW/UPDATE**. Needs `updateStudent` and `deleteStudent` (soft).
- `TeacherService`: **UPDATE**. Add `deleteTeacher` validation.
- `AcademicService`: **UPDATE**. Add `deleteSubject` validation.
- `DashboardService`: **UPDATE**. Implement `getTeacherPerformance` with real JPQL aggregation.

**Repositories**:
- `StudentRepository`: Needs filtering for `deleted=false`.
- `ClassRoutineRepository`: Needs `existsByTeacherId` and `existsBySubjectId`.
- `StudentMarksRepository`: Needs complex query for aggregation (Teacher Performance).
- `ClassSubjectRepository`: Used for linking Subjects to Teachers.

## Constitution Check

- [ ] **API-First**: Define `StudentRequest` / `StudentResponse` DTOs clearly.
- [ ] **Layered Arch**: Logic in `StudentService`, not Controller.
- [ ] **Security**: `delete` and `update` endpoints restricted to `ADMIN`.
- [ ] **Data Integrity**: Soft delete for Students; hard block for Teachers/Subjects if in Routine.
- [ ] **Testing**: Integration tests for "Block Deletion" scenarios.

## Phases

### Phase 0: Research & Validation
1.  **Check Student Entity**: Does it have a soft-delete flag?
2.  **Repo Check**: Verify `StudentMarksRepository` usage and existing queries.
3.  **Data Graph**: Confirm `StudentMarks` -> `ExamSchedule` -> `Subject` -> `ClassSubject` -> `Teacher` path is traversable.

### Phase 1: Design & Contracts
1.  **Data Model**: Update `data-model.md` if `Student` entity changes.
2.  **API Contract**: Define `PUT /api/students/{id}` and `DELETE /api/students/{id}` in `contracts/api.yaml` (or equivalent).
3.  **Quickstart**: Update docs if needed.

### Phase 2: Student Management
1.  **Service**: Implement `updateStudent` (handle fields safely).
2.  **Service**: Implement `deleteStudent` (soft delete + dependency check if needed).
3.  **Controller**: Expose endpoints `PUT` and `DELETE`.
4.  **Test**: `StudentIntegrationTest`.

### Phase 3: Operational Integrity
1.  **TeacherService**: Add `classRoutineRepository` dependency.
2.  **Validation**: Add strict check in `deleteTeacher`.
3.  **AcademicService**: Add similar check in `deleteSubject`.
4.  **Test**: `SchoolOperationsIntegrityTest` (Attempt delete, expect fail).

### Phase 4: Dashboard Real Data
1.  **Repository**: Add JPQL query to `StudentMarksRepository` or `TeacherRepository` to get avg marks per teacher.
2.  **Service**: Update `DashboardService.getTeacherPerformance` to use real data.
3.  **Test**: Verify dashboard returns non-mocked data.
