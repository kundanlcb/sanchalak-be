# Tasks: School Operations & Dashboard

**Branch**: `006-school-operations-dashboard`

## Phase 0: Refactoring
- [X] T001 Create `dto/academic/` package and move academic DTOs (`SubjectRequest`, `ClassSubjectRequest`, etc.)

## Phase 1: Enhanced Teacher Management (Priority P1)
- [X] T002 Update `Teacher` entity: Add `email`, `phone`, `qualification`, `profileImage`
- [X] T003 Update `Teacher` entity: Create Many-to-Many relationship with `Subject` (Specialization)
- [X] T004 Create `TeacherRequest` and `TeacherResponse` DTOs
- [X] T005 Implement `TeacherService` (Create: Link User, Assign Subjects)
- [X] T006 Implement `TeacherService` (Update, Delete with dependency check, Get All/ById)
- [X] T007 Create `TeacherController` with CRUD endpoints

## Phase 2: Academic Routine / Timetable (Priority P2)
- [X] T008 Create `ClassRoutine` entity (Day, Period, Class, Subject, Teacher, TimeSlot)
- [X] T009 Implement `RoutineService.getRoutine(classId)`: Return Matrix view
- [X] T010 Implement `RoutineService.assignSlot`: Add conflict validation (Double booking Teacher/Class)
- [X] T011 Implement `RoutineService.clearSlot(id)`
- [X] T012 Create `RoutineController` exposing `GET /routine` and `POST /routine`

## Phase 3: Financial & Academic CRUD (Priority P2)
- [X] T013 Update `AcademicService`: Add `updateClass`, `deleteClass` (with student check)
- [X] T014 Update `AcademicService`: Add `updateSubject`, `deleteSubject`
- [X] T015 Update `FinanceService`: Add `updateFeeStructure`, `updateFeeCategory`
- [X] T016 Update `FinanceService`: Add `deleteFeeStructure` (check payments), `deleteFeeCategory`
- [X] T017 Expose corresponding PUT/DELETE endpoints in `AcademicController` and `FinanceConfigController`

## Phase 4: Dashboard Analytics (Priority P1)
- [X] T018 Implement `DashboardService.getGenderDistribution()` (Male/Female count)
- [X] T019 Implement `DashboardService.getTeacherPerformance()` (Avg Student Marks per Teacher)
- [X] T020 Implement `DashboardService.getActivityFeed()` (Mocked simple events list)
- [X] T021 Expose new stats endpoints in `DashboardController`

## Phase 5: Testing
- [X] T022 Integration Test: Teacher Creation & Subject Linking
- [X] T023 Integration Test: Routine Conflict Validation (Ensure double booking fails)
- [X] T024 Integration Test: CRUD Deletion Safety (Ensure used entities cannot be deleted)

