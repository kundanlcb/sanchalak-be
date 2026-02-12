# Implementation Tasks: Attendance Service

**Spec**: [spec.md](spec.md) | **Branch**: `003-attendance-service`
**Phase**: Implementation

## Phase 1: Setup & Data Layer
*Goal: Initialize database schema and core entities.*

- [ ] T001 Create Enum `AttendanceStatus` in [src/main/java/com/cm/sanchalak/entity/AttendanceStatus.java](src/main/java/com/cm/sanchalak/entity/AttendanceStatus.java) (Values: PRESENT, ABSENT, LATE, EXCUSED, HOLIDAY)
- [ ] T002 Create `AttendanceRecord` Entity in [src/main/java/com/cm/sanchalak/entity/AttendanceRecord.java](src/main/java/com/cm/sanchalak/entity/AttendanceRecord.java) with Audit fields and `@Table(uniqueConstraints = ...)`
- [ ] T003 Create Flyway migration `V4__attendance_schema.sql` in [src/main/resources/db/migration/V4__attendance_schema.sql](src/main/resources/db/migration/V4__attendance_schema.sql)
- [ ] T004 Create `AttendanceRepository` in [src/main/java/com/cm/sanchalak/repository/AttendanceRepository.java](src/main/java/com/cm/sanchalak/repository/AttendanceRepository.java) with `findByStudentIdAndDate`, `existsByStudentIdAndDate`

## Phase 2: Foundation & DTOs
*Goal: Create data transfer objects required for API.*

- [ ] T005 [P] Create `MarkAttendanceRequest` DTO in [src/main/java/com/cm/sanchalak/dto/MarkAttendanceRequest.java](src/main/java/com/cm/sanchalak/dto/MarkAttendanceRequest.java)
- [ ] T006 [P] Create `BulkMarkAttendanceRequest` DTO in [src/main/java/com/cm/sanchalak/dto/BulkMarkAttendanceRequest.java](src/main/java/com/cm/sanchalak/dto/BulkMarkAttendanceRequest.java) with `List<StudentStatus>`
- [ ] T007 [P] Create `BulkMarkAttendanceResponse` DTO in [src/main/java/com/cm/sanchalak/dto/BulkMarkAttendanceResponse.java](src/main/java/com/cm/sanchalak/dto/BulkMarkAttendanceResponse.java)
- [ ] T008 [P] Create `AttendanceSummaryDto` in [src/main/java/com/cm/sanchalak/dto/AttendanceSummaryDto.java](src/main/java/com/cm/sanchalak/dto/AttendanceSummaryDto.java)
- [ ] T009 [P] Create `ClassAttendanceSheetDto` in [src/main/java/com/cm/sanchalak/dto/ClassAttendanceSheetDto.java](src/main/java/com/cm/sanchalak/dto/ClassAttendanceSheetDto.java)

## Phase 3: User Story 1 - Mark Daily Attendance (P1)
*Goal: Allow Teachers to submit attendance (Bulk & Single).*

- [ ] T010 [US1] Implement `markBulkAttendance` method in [src/main/java/com/cm/sanchalak/service/AttendanceService.java](src/main/java/com/cm/sanchalak/service/AttendanceService.java) (Transactional, Batch Save; Handle FR-003 "Default to Present")
- [ ] T011 [US1] Implement `markAttendance` (Single) method in [src/main/java/com/cm/sanchalak/service/AttendanceService.java](src/main/java/com/cm/sanchalak/service/AttendanceService.java) (Upsert logic)
- [ ] T012 [US1] Implement `getClassAttendanceSheet` method in [src/main/java/com/cm/sanchalak/service/AttendanceService.java](src/main/java/com/cm/sanchalak/service/AttendanceService.java)
- [ ] T013 [US1] Implement `AttendanceController` skeleton in [src/main/java/com/cm/sanchalak/controller/AttendanceController.java](src/main/java/com/cm/sanchalak/controller/AttendanceController.java)
- [ ] T014 [US1] Add `POST /api/attendance/bulk` endpoint to `AttendanceController` (Calls service markBulk)
- [ ] T015 [US1] Add `POST /api/attendance` endpoint to `AttendanceController` (Calls service markSingle)
- [ ] T016 [US1] Add `GET /api/attendance/class/{classId}/date/{date}` endpoint to `AttendanceController`

## Phase 4: User Story 2 - Student History (P2)
*Goal: Allow Students/Parents to view attendance history.*

- [ ] T017 [US2] Implement `getStudentAttendanceHistory` in [src/main/java/com/cm/sanchalak/service/AttendanceService.java](src/main/java/com/cm/sanchalak/service/AttendanceService.java)
- [ ] T018 [US2] Implement `getStudentAttendanceSummary` in [src/main/java/com/cm/sanchalak/service/AttendanceService.java](src/main/java/com/cm/sanchalak/service/AttendanceService.java) (Calculate %)
- [ ] T019 [US2] Add `GET /api/attendance` (History) to [src/main/java/com/cm/sanchalak/controller/AttendanceController.java](src/main/java/com/cm/sanchalak/controller/AttendanceController.java)
- [ ] T020 [US2] Add `GET /api/attendance/summary` to [src/main/java/com/cm/sanchalak/controller/AttendanceController.java](src/main/java/com/cm/sanchalak/controller/AttendanceController.java)

## Phase 4.5: User Story 3 - Admin Reports (P3)
*Goal: Allow Admins to identify attendance trends.*

- [ ] T023 [US3] Implement `getClassAttendanceStatistics` in [src/main/java/com/cm/sanchalak/service/AttendanceService.java](src/main/java/com/cm/sanchalak/service/AttendanceService.java) (Aggregated stats between dates)
- [ ] T024 [US3] Add `GET /api/attendance/class/{classId}/statistics` to [src/main/java/com/cm/sanchalak/controller/AttendanceController.java](src/main/java/com/cm/sanchalak/controller/AttendanceController.java)

## Phase 5: Security & Testing (Polish)
*Goal: Secure endpoints and verify correctness.*

- [ ] T021 Add `@PreAuthorize` (Teacher/Admin for Write, Student/Parent for Read Own) in `AttendanceController`
- [ ] T022 Implement Unit Tests (Mockito) and Integration Test `AttendanceIntegrationTest` in [src/test/java/com/cm/sanchalak/AttendanceIntegrationTest.java](src/test/java/com/cm/sanchalak/AttendanceIntegrationTest.java)

## Dependencies

- **Phase 3 (US1)** depends on **Phase 1 & 2**
- **Phase 4 (US2)** depends on **Phase 3** (need data to view history)
- **Phase 4.5 (US3)** depends on **Phase 3**
- **Phase 5** depends on **Phase 4.5**

## Implementation Strategy

1.  **MVP**: Complete Phases 1, 2, and 3. This enables the core "Teacher marks attendance" loop.
2.  **Next**: Phase 4 enables the Student view.
3.  **Final**: Security and Testing.
