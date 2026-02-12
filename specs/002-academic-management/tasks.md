# Tasks: Academic Management

**Feature Branch**: `002-academic-management`
**Spec**: [specs/002-academic-management/spec.md](spec.md)

## Dependencies
- **Blockers**: None (Core feature)
- **Story Order**: US1 > US2 > US3 > US4 (Linear dependency: Structure -> Marks -> Reports)

## Phase 1: Setup
Initialization tasks.

- [X] T001 Create Flyway V3 migration for academic tables in `src/main/resources/db/migration/V3__academic_schema.sql`

## Phase 2: Foundational
Creating core entities and repositories. Blocking for all stories.

- [X] T002 [P] Create `ExamTerm` entity in `src/main/java/com/cm/sanchalak/entity/ExamTerm.java`
- [X] T003 [P] Create `Subject` entity in `src/main/java/com/cm/sanchalak/entity/Subject.java`
- [X] T004 Create `ClassSubject` entity in `src/main/java/com/cm/sanchalak/entity/ClassSubject.java`
- [X] T005 Create `ExamSchedule` entity in `src/main/java/com/cm/sanchalak/entity/ExamSchedule.java`
- [X] T006 Create `StudentMarks` entity in `src/main/java/com/cm/sanchalak/entity/StudentMarks.java`
- [X] T007 [P] Create `Homework` entity in `src/main/java/com/cm/sanchalak/entity/Homework.java`
- [X] T008 [P] Create Repositories in `src/main/java/com/cm/sanchalak/repository/` (ExamTerm-, Subject-, ClassSubject-, ExamSchedule-, StudentMarks-, HomeworkRepository.java)

## Phase 3: User Story 1 - Exam & Subject Management
**Goal**: Allow Admins to define the academic structure.

- [X] T009 [US1] Create `AcademicService` structure in `src/main/java/com/cm/sanchalak/service/AcademicService.java`
- [X] T010 [US1] Implement `createExamTerm` and `getAllTerms` in `AcademicService`
- [X] T011 [US1] Implement `createSubject` and `getAllSubjects` in `AcademicService`
- [X] T012 [US1] Implement `assignSubjectToClass` in `AcademicService`
- [X] T013 [US1] Create `AcademicController` in `src/main/java/com/cm/sanchalak/controller/AcademicController.java`
- [X] T014 [US1] Implement endpoints for Terms, Subjects, and Class-Subject mapping in `AcademicController`

## Phase 4: User Story 2 - Marks Entry
**Goal**: Allow Teachers to enter marks with validation.
**Independent Test**: `MarkEntryIntegrationTest` (Verify constraints).

- [X] T015 [US2] Implement `scheduleExam` (create ExamSchedule) in `AcademicService` (Prerequisite for marks)
- [X] T016 [US2] Add Exam Schedule endpoint to `AcademicController`
- [X] T017 [US2] Create DTOs `MarkEntryRequest` and `MarkEntryResponse` in `src/main/java/com/cm/sanchalak/dto/`
- [X] T018 [P] [US2] Implement marks validation logic (Check obtained <= maxMarks) in `AcademicService`
- [X] T019 [US2] Implement `saveStudentMarks` in `AcademicService`
- [X] T020 [US2] Implement `POST /api/academic/marks` in `AcademicController`
- [X] T021 [US2] Create integration test `src/test/java/com/cm/sanchalak/MarkEntryIntegrationTest.java` verifying logic

## Phase 5: User Story 3 - Report Card Data
**Goal**: Aggregate student data for reports.

- [X] T022 [P] [US3] Create DTO `ReportCardDto` in `src/main/java/com/cm/sanchalak/dto/ReportCardDto.java`
- [X] T023 [US3] Implement `generateReportCard(studentId)` in `AcademicService` aggregation logic
- [X] T024 [US3] Implement `GET /api/academic/reports/{studentId}` in `AcademicController`

## Phase 6: User Story 4 - Homework Management
**Goal**: Allow Teachers to manage homework.

- [X] T025 [P] [US4] Create `HomeworkService` in `src/main/java/com/cm/sanchalak/service/HomeworkService.java`
- [X] T026 [US4] Implement create and fetch methods in `HomeworkService`
- [X] T027 [US4] Create `HomeworkController` in `src/main/java/com/cm/sanchalak/controller/HomeworkController.java` with CRUD endpoints

## Phase 7: Polish & Cross-Cutting
Finalizing the feature.

- [X] T028 Update `SecurityConfig` to protect new API paths (Teacher/Admin access)
- [X] T029 Clean up any unused imports or temporary classes

## Implementation Strategy
- **MVP**: Complete Phase 1-3 to get the structure running.
- **Data Integrity**: Phase 4 validation is critical; do not commit marks without checking `ExamSchedule.maxMarks`.
- **Parallelism**: Repositories (T008) and Homework (Phase 6) can be built in parallel with Core Academic logic.
