# Integration Gaps - Priority Order

Scope: `sanchalan` (web), `sanchalak_be` (backend), `gurukul` (student app), `guru` (teacher app)  
Goal: Implement backend support for FE features that are currently unsupported.

## P0 - Critical (must be done first)

1. Notice write operations (Create / Update / Delete / Mark as Read)
   - FE uses: `sanchalan/src/features/notices/services/noticeService.ts:50`
   - Backend has only read endpoints: `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/NoticeController.java:34`
   - Why P0: Admin/teacher communication workflows are incomplete without write APIs.

2. Document module APIs (upload/list/get/update/delete/verify/bulk-upload)
   - FE uses: `sanchalan/src/features/students/services/documentService.ts:53`
   - Backend: no document controller/routes present under `../sanchalak_be/src/main/java/com/cm/sanchalak/controller`
   - Why P0: Student onboarding/compliance flows cannot move to real backend.

3. Attendance modify API (`PUT /attendance/{id}`)
   - FE uses: `sanchalan/src/features/attendance/services/attendanceService.ts:95`
   - Backend controller has no update endpoint: `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/AttendanceController.java:22`
   - Why P0: Attendance correction is operationally mandatory in schools.

4. Student bulk import API (`/students/bulk-import`)
   - FE uses: `sanchalan/src/features/students/services/studentService.ts:108`
   - Backend student routes: `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/StudentController.java:21`
   - Why P0: Large data onboarding is blocked without bulk import.

## P1 - High (next sprint after P0)

1. Homework delete endpoint + class-filtered list behavior
   - FE expects delete + class filter: `sanchalan/src/features/homework/hooks/useHomework.ts:14`
   - Backend currently supports create/list only: `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/HomeworkController.java:22`
   - Why P1: Teacher workflows are partially blocked for assignment lifecycle.

2. Finance reporting gaps
   - Missing defaulters + transaction listing endpoints expected by FE:
   - `sanchalan/src/features/analytics/hooks/useFinancialReports.ts:24`
   - `sanchalan/src/features/finance/hooks/usePayment.ts:29`
   - Backend currently has ledger by student + record payment + receipt only:
   - `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/FinanceOperationsController.java:22`
   - Why P1: Finance dashboards and reconciliation are incomplete.

3. Marks query endpoint (`GET /marks` with filters)
   - FE uses marks list query: `sanchalan/src/features/academics/hooks/useMarks.ts:43`
   - Backend currently has only `POST /marks`: `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/AcademicController.java:91`
   - Why P1: Marks entry/review screens cannot fetch real data set.

## P2 - Medium (required for full teacher app rollout)

1. School-ops route set expected by FE (`/school-ops/*`)
   - FE expects: teachers/classes/routines in one module:
   - `sanchalan/src/features/school-ops/services/api.ts:6`
   - Backend currently exposes separate academics routes, and no class create/list API in controller set:
   - `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/TeacherController.java:15`
   - `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/RoutineController.java:15`
   - `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/AcademicController.java:111`
   - Why P2: Feature works only after endpoint mapping layer or new consolidated API.

2. Teacher self attendance (check-in/check-out) APIs for `guru`
   - FE flow exists in store: `../guru/src/features/academic/store/attendanceStore.ts:4`
   - Backend has student/class attendance only: `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/AttendanceController.java:22`
   - Why P2: Needed for teacher operational parity if this feature is part of launch scope.

3. Teacher dashboard aggregate endpoint for `guru`
   - FE needs dashboard cards/tasks: `../guru/src/features/dashboard/store/index.ts:28`
   - Backend `/api/me/home` is TODO/empty for teacher:
   - `../sanchalak_be/src/main/java/com/cm/sanchalak/controller/ProfileController.java:169`
   - Why P2: Teacher app home remains mock-only without server aggregation.

## Recommended Execution Sequence

1. Build all P0 endpoints + DTOs + auth rules.
2. Add integration tests for each P0 endpoint (success + role guard + validation).
3. Build P1 reporting/query APIs.
4. Decide strategy for P2 school-ops:
   - Option A: Add `/api/school-ops/*` backend module.
   - Option B: FE adapter layer to existing `/api/academic*` + `/api/academics/*`.
5. Complete teacher-specific APIs (`guru`) and remove corresponding mocks.

## Done Criteria (per feature)

1. Endpoint exists and matches FE request method/path.
2. Request/response schema mapped to FE type without client-side mock adapters.
3. Role-based authorization works for intended actors.
4. Error responses standardized for FE handling.
5. One integration test + one FE smoke flow verified.
