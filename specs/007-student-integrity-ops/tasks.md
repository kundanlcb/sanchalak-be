# Implementation Tasks - Student Integrity & Operations

## Phase 1: Student Management (Full CRUD + Soft Delete)
- [x] **Update Student Entity**
  - Add `deleted` boolean field.
  - Add `guardianName`, `guardianMobile`.
- [x] **Create DTOs**
  - Create `StudentRequest` (with validation).
  - Create `StudentResponse`.
- [x] **Update StudentRepository**
  - Add `findByDeletedFalse()`.
  - Add `countByDeletedFalse()`.
- [x] **Implement StudentService**
  - `createStudent`: Validate Class, set default deleted=false.
  - `updateStudent`: Handle field updates and Class transfer.
  - `deleteStudent`: Set `deleted=true` (Soft Delete).
  - `getAllStudents`: Return only non-deleted.
  - `getStudentById`: Return 404 if deleted.
- [x] **Create StudentController**
  - `POST /` (Admin).
  - `PUT /{id}` (Admin).
  - `DELETE /{id}` (Admin).
  - `GET /` (Admin/Teacher).

## Phase 2: Operational Integrity
- [x] **Teacher Integrity**
  - Update `ClassRoutineRepository`: Add `existsByTeacherId`.
  - Update `TeacherService.deleteTeacher`: Block if in Routine.
- [x] **Subject Integrity**
  - Update `ClassRoutineRepository`: Add `existsBySubjectId`.
  - Update `AcademicService.deleteSubject`: Block if in Routine or Class.

## Phase 3: Dashboard Real Data
- [x] **Stats Integation**
  - Use `countByDeletedFalse()` for Students/Teachers.
- [x] **Attendance Integration**
  - Add `countByDateAndStatus` to `AttendanceRepository`.
  - Calculate daily attendance % in `getStats`.
- [ ] **Teacher Performance (Analytics)**
  - Implement repository query for Avg Marks per Teacher.
  - Update `getTeacherPerformance` to use real data.

## Phase 4: Verification
- [ ] **Integration Tests**
  - Test Soft Delete (Student).
  - Test Integrity Block (Teacher/Subject).
  - Test Dashboard Data.
- [ ] **Manual Verification**
  - Verify endpoints via Swagger/Postman.
