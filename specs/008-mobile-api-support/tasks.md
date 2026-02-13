# Tasks: Mobile API Backend Support

**Feature**: 008-mobile-api-support  
**Input**: Design documents from `/specs/008-mobile-api-support/`  
**Generated**: 2026-02-13

## Task Format

`- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Parallelizable (different files, no blocking dependencies)
- **[Story]**: US1-US6 for user story phases
- **File paths**: Absolute when possible, relative to `src/main/java/com/cm/sanchalak/` for Java code

---

## Phase 1: Setup (Project Initialization)

**Purpose**: Project structure and dependency configuration

- [X] T001 Update build.gradle with Firebase Admin SDK dependency for FCM/APNs push notifications
- [X] T002 [P] Update build.gradle with file storage provider dependencies (AWS SDK S3 or Azure Storage Blob, configurable)
- [X] T003 [P] Update build.gradle with Spring Cache + Caffeine dependency for parent-child linkage caching
- [X] T004 Update application.properties with JWT access token expiry (900000ms = 15 min) and refresh token expiry (2592000000ms = 30 days)
- [X] T005 [P] Update application.properties with OTP settings (expiry=300s, rate limit=3/15min, encryption key)
- [X] T006 [P] Update application.properties with FCM credentials path and async executor pool config
- [X] T007 [P] Update application.properties with file storage provider config (provider type, bucket/container name, region, presigned URL expiry)
- [X] T008 [P] Add firebase-adminsdk.json to src/main/resources/ (dev credentials, replace in production)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story implementation

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database Migrations

- [X] T009 Create Flyway migration V8__add_role_parent.sql to add ROLE_PARENT to role enum
- [X] T010 Create Flyway migration V9__add_student_user_id.sql to add user_id column (BIGINT NULL FK) to students table with unique index
- [X] T011 Create Flyway migration V10__create_parent_tables.sql for parents and parent_student_links tables with indexes
- [X] T012 Create Flyway migration V11__create_auth_tables.sql for otp_verifications and refresh_tokens tables with indexes
- [X] T013 Create Flyway migration V12__create_homework_submission.sql for homework_submissions table with indexes
- [X] T014 Create Flyway migration V13__create_transport_tables.sql for Vehicle, Route, Stop, Trip, StudentTransportAssignment, LocationPing (partitioned), TransportEvent tables with indexes
- [X] T015 Create Flyway migration V14__create_notification_tables.sql for notification_tokens and notification_logs tables
- [X] T016 Create Flyway migration V15__create_notice_tables.sql for notices and notice_read_status tables (if Notice entity doesn't exist)

### Core Entity Models (Modified Entities)

- [X] T017 Update RoleName.java enum to add ROLE_PARENT value in src/main/java/com/cm/sanchalak/entity/RoleName.java
- [X] T018 Update Student.java entity to add userId field (nullable, unique FK to User) in src/main/java/com/cm/sanchalak/entity/Student.java

### Core Entity Models (New Auth Entities)

- [X] T019 [P] Create OtpVerification.java entity in src/main/java/com/cm/sanchalak/entity/OtpVerification.java
- [X] T020 [P] Create RefreshToken.java entity in src/main/java/com/cm/sanchalak/entity/RefreshToken.java

### Core Repositories (Auth)

- [X] T021 [P] Create OtpVerificationRepository.java in src/main/java/com/cm/sanchalak/repository/OtpVerificationRepository.java
- [X] T022 [P] Create RefreshTokenRepository.java in src/main/java/com/cm/sanchalak/repository/RefreshTokenRepository.java

### Core Services (Auth & Encryption)

- [X] T023 Create OtpService.java for OTP generation (SecureRandom 6-digit), AES-256 encryption, expiry validation, rate limiting in src/main/java/com/cm/sanchalak/service/OtpService.java
- [X] T024 Create RefreshTokenService.java for token rotation (one-time use, BCrypt hashing, family tracking) in src/main/java/com/cm/sanchalak/service/RefreshTokenService.java
- [X] T025 Update SecurityFilterChain configuration to permit /api/mobile/v1/auth/request-otp and /api/mobile/v1/auth/verify-otp endpoints in src/main/java/com/cm/sanchalak/config/SecurityConfig.java

### Core DTOs (Auth)

- [X] T026 [P] Create OtpRequestDto.java in src/main/java/com/cm/sanchalak/dto/OtpRequestDto.java
- [X] T027 [P] Create OtpVerifyDto.java in src/main/java/com/cm/sanchalak/dto/OtpVerifyDto.java
- [X] T028 [P] Create AuthTokenResponseDto.java in src/main/java/com/cm/sanchalak/dto/AuthTokenResponseDto.java
- [X] T029 [P] Create RefreshTokenRequestDto.java in src/main/java/com/cm/sanchalak/dto/RefreshTokenRequestDto.java

### API Response Envelope

- [X] T030 Create ApiResponse.java generic wrapper with success, data, error, meta fields in src/main/java/com/cm/sanchalak/dto/ApiResponse.java
- [X] T031 [P] Create ApiError.java with code, message, details fields in src/main/java/com/cm/sanchalak/dto/ApiError.java
- [X] T032 [P] Create ApiMeta.java with requestId, timestamp, pagination fields in src/main/java/com/cm/sanchalak/dto/ApiMeta.java
- [X] T033 Create GlobalExceptionHandler.java for mobile API error handling with user-friendly messages in src/main/java/com/cm/sanchalak/exception/MobileApiExceptionHandler.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Student OTP Login and Profile Access (P1) 🎯 MVP

**Goal**: Enable students to login via OTP and view personalized profile/dashboard

**Independent Test**: Send OTP to registered student mobile, verify OTP, receive JWT tokens, call /me endpoint to get student profile, call /me/home to get dashboard data

### Implementation for User Story 1

- [X] T034 [US1] Implement POST /api/mobile/v1/auth/request-otp in MobileAuthController.java (delegates to OtpService)
- [X] T035 [US1] Implement POST /api/mobile/v1/auth/verify-otp in MobileAuthController.java (validates OTP, generates JWT tokens, returns AuthTokenResponseDto)
- [X] T036 [US1] Implement POST /api/mobile/v1/auth/refresh in MobileAuthController.java (validates refresh token, rotates tokens)
- [X] T037 [US1] Implement POST /api/mobile/v1/auth/logout in MobileAuthController.java (revokes refresh token)
- [X] T038 [P] [US1] Create UserProfileDto.java in src/main/java/com/cm/sanchalak/dto/UserProfileDto.java
- [X] T039 [P] [US1] Create DashboardDto.java in src/main/java/com/cm/sanchalak/dto/DashboardDto.java
- [X] T040 [US1] Create DashboardAggregationService.java to aggregate attendance, homework count, next exam, pending fees, recent notices in src/main/java/com/cm/sanchalak/service/DashboardAggregationService.java
- [X] T041 [US1] Implement GET /api/mobile/v1/me in MobileStudentController.java (returns current user profile with auto-resolved studentId for STUDENT role)
- [X] T042 [US1] Implement GET /api/mobile/v1/me/home in MobileStudentController.java (calls DashboardAggregationService for STUDENT role)
- [X] T043 [US1] Add @PreAuthorize("hasAnyRole('STUDENT', 'PARENT')") to MobileStudentController endpoints
- [X] T044 [US1] Add logging for OTP requests and JWT issuance with mobile number, userId, timestamp, IP in OtpService and RefreshTokenService

**Checkpoint**: Student OTP login and dashboard functional and independently testable

---

## Phase 4: User Story 2 - Parent Multi-Child Account Access (P1) 🎯 MVP

**Goal**: Enable parents to login, view linked children, and access each child's data with proper authorization

**Independent Test**: Create parent account linked to 2 students, authenticate via OTP, call /me/students to get child list, fetch attendance/homework for each child with parent authorization

### Entity Models for User Story 2

- [X] T045 [P] [US2] Create Parent.java entity in src/main/java/com/cm/sanchalak/entity/Parent.java
- [X] T046 [P] [US2] Create ParentStudentLink.java entity in src/main/java/com/cm/sanchalak/entity/ParentStudentLink.java

### Repositories for User Story 2

- [X] T047 [P] [US2] Create ParentRepository.java in src/main/java/com/cm/sanchalak/repository/ParentRepository.java
- [X] T048 [P] [US2] Create ParentStudentLinkRepository.java with custom query findActiveByParentIdAndStudentId in src/main/java/com/cm/sanchalak/repository/ParentStudentLinkRepository.java

### Services for User Story 2

- [X] T049 [US2] Create ParentService.java to fetch parent profile and linked students in src/main/java/com/cm/sanchalak/service/ParentService.java
- [X] T050 [US2] Create ParentAuthorizationService.java with @Cacheable(cacheNames="parent-linkage", key="#parentId+'_'+#studentId") for linkage validation in src/main/java/com/cm/sanchalak/service/ParentAuthorizationService.java
- [X] T051 [US2] Configure Caffeine cache with 1-hour TTL for parent-linkage cache in src/main/java/com/cm/sanchalak/config/CacheConfig.java

### DTOs for User Story 2

- [X] T052 [P] [US2] Create LinkedStudentDto.java in src/main/java/com/cm/sanchalak/dto/LinkedStudentDto.java

### Implementation for User Story 2

- [X] T053 [US2] Extend GET /api/mobile/v1/me to return parentId for PARENT role in MobileStudentController.java
- [X] T054 [US2] Implement GET /api/mobile/v1/me/students in MobileStudentController.java (calls ParentService to get linked children, PARENT role only)
- [X] T055 [US2] Extend GET /api/mobile/v1/me/home to accept studentId query param for PARENT role and validate linkage via ParentAuthorizationService
- [X] T056 [US2] Add linkage validation in DashboardAggregationService before fetching data for parent-requested studentId
- [X] T057 [US2] Add 403 Forbidden error handling for unauthorized parent-student access attempts in MobileApiExceptionHandler.java

**Checkpoint**: Parent multi-child access functional, authorization enforced, independently testable

---

## Phase 5: User Story 4 - Mobile-Optimized Wrapper Endpoints (P2)

**Goal**: Provide mobile-optimized wrapper endpoints over existing APIs with parent authorization

**Independent Test**: Authenticate as student or parent, call wrapper endpoints (/attendance/summary, /homework, /fees/ledger, /timetable, /results) and verify mobile response format with auto-resolved studentId for students and linkage validation for parents

### DTOs for User Story 4

- [X] T058 [P] [US4] Create AttendanceSummaryDto.java in src/main/java/com/cm/sanchalak/dto/mobile/AttendanceSummaryDto.java
- [X] T059 [P] [US4] Create HomeworkListDto.java in src/main/java/com/cm/sanchalak/dto/mobile/HomeworkListDto.java
- [X] T060 [P] [US4] Create FeeLedgerDto.java in src/main/java/com/cm/sanchalak/dto/mobile/FeeLedgerDto.java
- [X] T061 [P] [US4] Create TimetableDto.java in src/main/java/com/cm/sanchalak/dto/mobile/TimetableDto.java
- [X] T062 [P] [US4] Create ResultsDto.java in src/main/java/com/cm/sanchalak/dto/mobile/ResultsDto.java

### Controllers for User Story 4 (Wrappers)

- [X] T063 [P] [US4] Create MobileAttendanceController.java with GET /api/mobile/v1/attendance/summary and GET /api/mobile/v1/attendance/history (wraps existing AttendanceController) in src/main/java/com/cm/sanchalak/controller/mobile/MobileAttendanceController.java
- [X] T064 [P] [US4] Create MobileHomeworkController.java with GET /api/mobile/v1/homework (wraps existing HomeworkController) in src/main/java/com/cm/sanchalak/controller/mobile/MobileHomeworkController.java
- [X] T065 [P] [US4] Create MobileFeesController.java with GET /api/mobile/v1/fees/ledger, POST /api/mobile/v1/fees/pay, GET /api/mobile/v1/fees/receipt/{receiptId} (wraps existing FinanceOperationsController) in src/main/java/com/cm/sanchalak/controller/mobile/MobileFeesController.java
- [X] T066 [P] [US4] Create MobileTimetableController.java with GET /api/mobile/v1/timetable (wraps existing RoutineController) in src/main/java/com/cm/sanchalak/controller/mobile/MobileTimetableController.java
- [X] T067 [P] [US4] Create MobileResultsController.java with GET /api/mobile/v1/results (wraps existing AcademicController) in src/main/java/com/cm/sanchalak/controller/mobile/MobileResultsController.java

### Authorization Logic for User Story 4

- [X] T068 [US4] Add auto-resolve studentId from JWT for STUDENT role in all wrapper controllers
- [X] T069 [US4] Add parent linkage validation via ParentAuthorizationService for PARENT role in all wrapper controllers
- [X] T070 [US4] Add 403 Forbidden error with error code AUTHZ_001 for unauthorized parent access in wrapper controllers

**Checkpoint**: All wrapper endpoints functional, parent authorization enforced, mobile-optimized responses delivered

---

## Phase 6: User Story 5 - Homework Submission and Completion Tracking (P2)

**Goal**: Enable students to upload homework submissions, track status, and allow teachers/parents to view submissions

**Independent Test**: Create homework assignment for a class, authenticate as student, upload file via POST /homework/{id}/submit, verify submission recorded with S3 file URL, retrieve submission via GET /homework/{id}/submission

### Entity Models for User Story 5

- [X] T071 [P] [US5] Create HomeworkSubmission.java entity with submissionFileUrls as JSON array in src/main/java/com/cm/sanchalak/entity/HomeworkSubmission.java

### Repositories for User Story 5

- [X] T072 [P] [US5] Create HomeworkSubmissionRepository.java with query findByHomeworkIdAndStudentId in src/main/java/com/cm/sanchalak/repository/HomeworkSubmissionRepository.java

### Services for User Story 5

- [X] T073 [US5] Create FileStorageService interface in src/main/java/com/cm/sanchalak/service/storage/FileStorageService.java with methods: generateUploadUrl(), generateDownloadUrl(), deleteFile()
- [X] T073a [P] [US5] Create S3StorageProvider.java implementation for AWS S3 in src/main/java/com/cm/sanchalak/service/storage/impl/S3StorageProvider.java
- [X] T073b [P] [US5] Create AzureBlobStorageProvider.java implementation for Azure Blob Storage in src/main/java/com/cm/sanchalak/service/storage/impl/AzureBlobStorageProvider.java
- [X] T073c [US5] Create FileStorageConfig.java with @ConditionalOnProperty to select active provider based on application.properties in src/main/java/com/cm/sanchalak/config/FileStorageConfig.java
- [X] T074 [US5] Create HomeworkSubmissionService.java to handle submission creation, file URL storage (delegates to FileStorageService), resubmission (overwrite), and late status marking in src/main/java/com/cm/sanchalak/service/HomeworkSubmissionService.java

### DTOs for User Story 5

- [X] T075 [P] [US5] Create HomeworkSubmissionDto.java in src/main/java/com/cm/sanchalak/dto/HomeworkSubmissionDto.java
- [X] T076 [P] [US5] Create PresignedUrlDto.java in src/main/java/com/cm/sanchalak/dto/PresignedUrlDto.java

### Implementation for User Story 5

- [X] T077 [US5] Add POST /api/mobile/v1/homework/{id}/submit endpoint in MobileHomeworkController.java (accepts multipart file, delegates to HomeworkSubmissionService, returns submission confirmation)
- [X] T078 [US5] Add GET /api/mobile/v1/homework/{id}/submission endpoint in MobileHomeworkController.java (returns submission details with file URLs for STUDENT role or PARENT with linkage validation)
- [X] T079 [US5] Add @PreAuthorize("hasRole('STUDENT')") to POST /homework/{id}/submit (only students can submit)
- [X] T080 [US5] Add validation for file size (max 10MB) and file types (jpg, png, pdf) in HomeworkSubmissionService
- [X] T081 [US5] Add late submission detection by comparing submission timestamp with homework.dueDate in HomeworkSubmissionService
- [X] T082 [US5] Extend GET /api/mobile/v1/homework endpoint to include submissionStatus per student
- [X] T082a [US5] Add provider switching documentation in quickstart.md showing how to toggle between AWS S3 and Azure Blob Storage via application.properties

**Checkpoint**: Homework submission functional, file storage provider working (S3 or Azure), late status tracked, independently testable

---

## Phase 7: User Story 3 - Bus Live Tracking for Student Route (P2)

**Goal**: Enable students/parents to view assigned bus route, track live location, see ETA for stops, and view pickup/drop event history

**Independent Test**: Create bus route with vehicle and stops, assign student to route, simulate GPS pings via POST /transport/location-pings, call GET /transport/live to get current location, verify ETA calculation for stops

### Entity Models for User Story 3

- [X] T083 [P] [US3] Create Vehicle.java entity in src/main/java/com/cm/sanchalak/entity/Vehicle.java
- [X] T084 [P] [US3] Create Route.java entity in src/main/java/com/cm/sanchalak/entity/Route.java
- [X] T085 [P] [US3] Create Stop.java entity in src/main/java/com/cm/sanchalak/entity/Stop.java
- [X] T086 [P] [US3] Create Trip.java entity in src/main/java/com/cm/sanchalak/entity/Trip.java
- [X] T087 [P] [US3] Create StudentTransportAssignment.java entity in src/main/java/com/cm/sanchalak/entity/StudentTransportAssignment.java
- [X] T088 [P] [US3] Create LocationPing.java entity with capturedAt and receivedAt timestamps in src/main/java/com/cm/sanchalak/entity/LocationPing.java
- [X] T089 [P] [US3] Create TransportEvent.java entity in src/main/java/com/cm/sanchalak/entity/TransportEvent.java

### Repositories for User Story 3

- [X] T090 [P] [US3] Create VehicleRepository.java in src/main/java/com/cm/sanchalak/repository/VehicleRepository.java
- [X] T091 [P] [US3] Create RouteRepository.java in src/main/java/com/cm/sanchalak/repository/RouteRepository.java
- [X] T092 [P] [US3] Create StopRepository.java with query findByRouteIdOrderByStopOrder in src/main/java/com/cm/sanchalak/repository/StopRepository.java
- [X] T093 [P] [US3] Create TripRepository.java with query findActiveByRouteIdAndTripDate in src/main/java/com/cm/sanchalak/repository/TripRepository.java
- [X] T094 [P] [US3] Create StudentTransportAssignmentRepository.java with query findActiveByStudentId in src/main/java/com/cm/sanchalak/repository/StudentTransportAssignmentRepository.java
- [X] T095 [P] [US3] Create LocationPingRepository.java with query findLatestByVehicleId (max receivedAt within 2 min) in src/main/java/com/cm/sanchalak/repository/LocationPingRepository.java
- [X] T096 [P] [US3] Create TransportEventRepository.java with query findByTripIdAndStudentId in src/main/java/com/cm/sanchalak/repository/TransportEventRepository.java

### Services for User Story 3

- [X] T097 [US3] Create TransportService.java to fetch route, vehicle, stops, and student assignment details in src/main/java/com/cm/sanchalak/service/TransportService.java
- [X] T098 [US3] Create LocationTrackingService.java for GPS ping ingestion, staleness detection (2-min threshold), and latest location retrieval in src/main/java/com/cm/sanchalak/service/LocationTrackingService.java
- [X] T099 [US3] Create TransportEtaService.java to calculate ETA using Haversine distance formula, current speed, and scheduled time fallback in src/main/java/com/cm/sanchalak/service/TransportEtaService.java
- [X] T100 [US3] Configure @Cacheable(cacheNames="route-assignments", key="#studentId") with 6-hour TTL for student transport assignments in CacheConfig.java

### DTOs for User Story 3

- [X] T101 [P] [US3] Create RouteDetailsDto.java in src/main/java/com/cm/sanchalak/dto/RouteDetailsDto.java (unified architecture - no mobile directory)
- [X] T102 [P] [US3] Create LiveLocationDto.java with stale boolean flag in src/main/java/com/cm/sanchalak/dto/LiveLocationDto.java (unified architecture)
- [X] T103 [P] [US3] Create StopEtaDto.java with estimatedArrivalMinutes field in src/main/java/com/cm/sanchalak/dto/StopEtaDto.java (unified architecture)
- [X] T104 [P] [US3] Create TransportEventDto.java in src/main/java/com/cm/sanchalak/dto/TransportEventDto.java (unified architecture)
- [X] T105 [P] [US3] Create LocationPingDto.java in src/main/java/com/cm/sanchalak/dto/LocationPingDto.java

### Implementation for User Story 3

- [X] T106 [P] [US3] Create TransportController.java in src/main/java/com/cm/sanchalak/controller/TransportController.java (unified architecture - no mobile directory)
- [X] T107 [US3] Implement GET /api/transport/my-route in TransportController (returns assigned route details for STUDENT or PARENT with linkage validation)
- [X] T108 [US3] Implement GET /api/transport/live?routeId={id} in TransportController (returns latest location ping with stale flag)
- [X] T109 [US3] Implement GET /api/transport/stops?routeId={id} in TransportController (returns ordered stop list with ETA calculated by TransportEtaService)
- [X] T110 [US3] Implement GET /api/transport/events?studentId={id}&date={date} in TransportController (returns pickup/drop event history)
- [X] T111 [US3] Implement POST /api/transport/location-pings for GPS device ingestion (authenticate with device API key, not JWT) in TransportController
- [X] T112 [US3] Implement TransportEventService.java for manual/device event logging in src/main/java/com/cm/sanchalak/service/TransportEventService.java
- [X] T113 [US3] Add authorization: STUDENT auto-resolves to assigned route, PARENT validates linkage before accessing child's route
- [X] T114 [US3] Add 403 Forbidden error for unauthorized transport data access

**Checkpoint**: Bus tracking functional, ETA calculation working, authorization enforced, independently testable

---

## Phase 8: User Story 6 - Push Notifications for Critical Events (P3)

**Goal**: Enable device token registration and send push notifications via FCM/APNs for critical events (absence, fee due, notice, bus alert)

**Independent Test**: Register FCM token via POST /notifications/register, trigger absence event for student, verify FCM payload sent and NotificationLog created, check device notification tray

### Entity Models for User Story 6

- [X] T115 [P] [US6] Create NotificationToken.java entity in src/main/java/com/cm/sanchalak/entity/NotificationToken.java
- [X] T116 [P] [US6] Create NotificationLog.java entity in src/main/java/com/cm/sanchalak/entity/NotificationLog.java

### Repositories for User Story 6

- [X] T117 [P] [US6] Create NotificationTokenRepository.java with query findActiveByUserId in src/main/java/com/cm/sanchalak/repository/NotificationTokenRepository.java
- [X] T118 [P] [US6] Create NotificationLogRepository.java in src/main/java/com/cm/sanchalak/repository/NotificationLogRepository.java

### Services for User Story 6

- [X] T119 [US6] Create NotificationService.java with @Async methods for FCM/APNs push sending using Firebase Admin SDK in src/main/java/com/cm/sanchalak/service/NotificationService.java
- [X] T120 [US6] Configure @EnableAsync and ThreadPoolTaskExecutor with core-pool-size=5, max-pool-size=10 in src/main/java/com/cm/sanchalak/config/AsyncConfig.java
- [X] T121 [US6] Add Firebase Admin SDK initialization with credentials from firebase-adminsdk.json in NotificationService

### DTOs for User Story 6

- [X] T122 [P] [US6] Create NotificationTokenDto.java in src/main/java/com/cm/sanchalak/dto/NotificationTokenDto.java
- [X] T123 [P] [US6] Create PushNotificationDto.java in src/main/java/com/cm/sanchalak/dto/PushNotificationDto.java

### Implementation for User Story 6

- [X] T124 [P] [US6] Create NotificationController.java in src/main/java/com/cm/sanchalak/controller/mobile/NotificationController.java
- [X] T125 [US6] Implement POST /api/mobile/v1/notifications/register in NotificationController (stores FCM/APNs token linked to userId)
- [X] T126 [US6] Implement POST /api/mobile/v1/notifications/unregister in NotificationController (marks token as inactive)
- [X] T127 [US6] Add event listeners for critical events: absence (@EventListener), fee due reminder (scheduled job), new notice, bus alert (proximity trigger)
- [X] T128 [US6] Implement absence notification: when attendance status=ABSENT, send push to parent's registered devices via NotificationService
- [X] T129 [US6] Implement fee due reminder: scheduled job runs daily at 9 AM, checks fees due in 3 days, sends push to parents
- [X] T130 [US6] Implement notice notification: when Notice priority=HIGH is created, send push to all parent/student devices (filter by targetRole)
- [X] T131 [US6] Implement bus alert notification: when bus location is within 2km of student's pickup stop, send push to parent/student
- [X] T132 [US6] Add NotificationLog entry for every push sent with deliveryStatus (sent/delivered/failed)

**Checkpoint**: Push notifications functional, FCM integration working, event triggers connected, independently testable

---

## Phase 9: Additional Features (Notices & Calendar)

**Purpose**: Implement notice system and calendar aggregation endpoint

### Entity Models for Notices (if not exists)

- [X] T133 [P] Create Notice.java entity in src/main/java/com/cm/sanchalak/entity/Notice.java (skip if already exists)
- [X] T134 [P] Create NoticeReadStatus.java entity in src/main/java/com/cm/sanchalak/entity/NoticeReadStatus.java (skip if already exists)

### Repositories for Notices

- [X] T135 [P] Create NoticeRepository.java with query findByTargetRoleAndPublishDateBetween in src/main/java/com/cm/sanchalak/repository/NoticeRepository.java
- [X] T136 [P] Create NoticeReadStatusRepository.java with query existsByUserIdAndNoticeId in src/main/java/com/cm/sanchalak/repository/NoticeReadStatusRepository.java

### Services for Notices

- [X] T137 Create NoticeService.java to fetch notices filtered by role and mark as read in src/main/java/com/cm/sanchalak/service/NoticeService.java

### DTOs for Notices

- [X] T138 [P] Create NoticeDto.java with readStatus boolean in src/main/java/com/cm/sanchalak/dto/mobile/NoticeDto.java
- [X] T139 [P] Create NoticeDetailDto.java in src/main/java/com/cm/sanchalak/dto/mobile/NoticeDetailDto.java

### Implementation for Notices

- [X] T140 [P] Create MobileNoticeController.java in src/main/java/com/cm/sanchalak/controller/mobile/MobileNoticeController.java
- [X] T141 Implement GET /api/mobile/v1/notices in MobileNoticeController (returns notices filtered by role with read status)
- [X] T142 Implement GET /api/mobile/v1/notices/{id} in MobileNoticeController (returns notice details, marks as read via NoticeReadStatus entry)

### Implementation for Calendar

- [X] T143 [P] Create MobileCalendarController.java in src/main/java/com/cm/sanchalak/controller/mobile/MobileCalendarController.java
- [X] T144 [P] Create CalendarEventDto.java in src/main/java/com/cm/sanchalak/dto/mobile/CalendarEventDto.java
- [X] T145 Create CalendarAggregationService.java to merge exam schedules, holidays, notice dates in src/main/java/com/cm/sanchalak/service/CalendarAggregationService.java
- [X] T146 Implement GET /api/mobile/v1/calendar in MobileCalendarController (returns aggregated events for STUDENT or PARENT with linked children)

**Checkpoint**: Notices and calendar functional, read tracking implemented

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Finalize error handling, logging, caching, testing, and documentation

### Error Handling & Logging

- [X] T147 Add correlation/request ID generation and propagation in all mobile API responses via LoggingFilter in src/main/java/com/cm/sanchalak/filter/RequestIdFilter.java
- [X] T148 Add audit logging for authentication attempts (success/failure) with mobile number, userId, timestamp, IP, user agent in OtpService
- [X] T149 Add audit logging for financial transactions with userId, studentId, amount, transactionId, status in FinanceService wrapper
- [X] T150 Add audit logging for parent-student data access requests with parentId, studentId, endpoint, timestamp in ParentAuthorizationService
- [X] T151 Add rate limiting interceptor for OTP request/verify endpoints (3 requests per 15 min) in src/main/java/com/cm/sanchalak/interceptor/RateLimitInterceptor.java
- [X] T152 Add @ControllerAdvice exception handler for 400, 401, 403, 404, 500 errors with standardized ApiError response in MobileApiExceptionHandler

### Testing

- [X] T153 [P] Write unit tests for OtpService (OTP generation, encryption, expiry, rate limiting) in src/test/java/com/cm/sanchalak/service/OtpServiceTest.java
- [X] T154 [P] Write unit tests for RefreshTokenService (token rotation, BCrypt validation) in src/test/java/com/cm/sanchalak/service/RefreshTokenServiceTest.java
- [X] T155 [P] Write unit tests for ParentAuthorizationService (linkage validation, cache behavior) in src/test/java/com/cm/sanchalak/service/ParentAuthorizationServiceTest.java
- [X] T156 [P] Write unit tests for TransportEtaService (Haversine distance, ETA calculation, staleness detection) in src/test/java/com/cm/sanchalak/service/TransportEtaServiceTest.java
- [X] T157 [P] Write unit tests for HomeworkSubmissionService (submission creation, resubmission, late status) with mocked FileStorageService in src/test/java/com/cm/sanchalak/service/HomeworkSubmissionServiceTest.java
- [ ] T157a [P] Write unit tests for S3StorageProvider (presigned URL generation, expiry validation) in src/test/java/com/cm/sanchalak/service/storage/impl/S3StorageProviderTest.java
- [ ] T157b [P] Write unit tests for AzureBlobStorageProvider (SAS token generation) in src/test/java/com/cm/sanchalak/service/storage/impl/AzureBlobStorageProviderTest.java
- [X] T158 [P] Write integration test for OTP authentication flow (request OTP → verify OTP → receive tokens → access protected endpoint) in src/test/java/com/cm/sanchalak/integration/OtpAuthenticationFlowTest.java
- [ ] T159 [P] Write integration test for parent multi-child access (authenticate as parent → get children → access child data) in src/test/java/com/cm/sanchalak/integration/ParentMultiChildAccessTest.java
- [ ] T160 [P] Write integration test for transport live tracking (create route → simulate GPS pings → fetch live location → verify ETA) in src/test/java/com/cm/sanchalak/integration/TransportLiveTrackingTest.java
- [X] T161 [P] Write controller tests for MobileAuthController with @SpringBootTest and MockMvc in src/test/java/com/cm/sanchalak/controller/MobileAuthControllerTest.java
- [ ] T162 [P] Write controller tests for MobileStudentController with JWT authentication in src/test/java/com/cm/sanchalak/controller/MobileStudentControllerTest.java
- [ ] T163 [P] Write controller tests for TransportController with JWT authentication and parent authorization in src/test/java/com/cm/sanchalak/controller/TransportControllerTest.java

### Documentation

- [X] T164 Add Springdoc @Operation and @ApiResponse annotations to all MobileAuthController endpoints in src/main/java/com/cm/sanchalak/controller/mobile/MobileAuthController.java (annotated AuthController)
- [ ] T165 [P] Add Springdoc @Operation and @ApiResponse annotations to all MobileStudentController endpoints in src/main/java/com/cm/sanchalak/controller/mobile/MobileStudentController.java
- [X] T166 [P] Add Springdoc @Operation and @ApiResponse annotations to all TransportController endpoints in src/main/java/com/cm/sanchalak/controller/mobile/TransportController.java
- [ ] T167 [P] Add Springdoc @Operation and @ApiResponse annotations to all MobileHomeworkController endpoints in src/main/java/com/cm/sanchalak/controller/mobile/MobileHomeworkController.java
- [ ] T168 [P] Add Springdoc @Operation and @ApiResponse annotations to all wrapper controllers (Attendance, Fees, Timetable, Results) in src/main/java/com/cm/sanchalak/controller/mobile/
- [ ] T169 Generate OpenAPI 3.0 YAML specification via Springdoc and save to specs/008-mobile-api-support/contracts/mobile-api-v1.yaml
- [X] T170 Update README.md with mobile API setup instructions, OTP flow, parent authorization, testing guide

### Performance Optimization

- [ ] T171 Add database indexes for performance: idx_otp_mobile_number, idx_refresh_token_user_id, idx_parent_student_link_composite, idx_location_ping_vehicle_received_at, idx_student_transport_assignment_student_id in migration scripts
- [X] T172 Configure Spring Cache with Caffeine for parent-linkage cache (1-hour TTL) and route-assignments cache (6-hour TTL) in CacheConfig.java
- [ ] T173 Add pagination support for GET /api/mobile/v1/homework, GET /api/mobile/v1/notices, GET /api/mobile/v1/attendance/history with pageSize and pageNumber params
- [ ] T174 Add ETag and Last-Modified headers for cacheable responses (attendance summary, timetable, results) in wrapper controllers
- [ ] T175 Configure Postgres partitioning for location_pings table by date with 30-day retention policy (daily partition drops) in V13 migration or separate job

### Security Enhancements

- [X] T176 Add PII masking for mobile numbers in logs (log only last 4 digits) in LoggingFilter (and AuthController)
- [X] T177 Add encryption configuration for OTP storage using AES-256 with key from application.properties in OtpService
- [X] T178 Add BCrypt hashing for refresh tokens before storage in RefreshTokenService
- [ ] T179 Add HTTPS enforcement and HSTS headers via SecurityFilterChain for /api/mobile/v1/* endpoints
- [X] T180 Add CORS configuration to allow mobile app origins (localhost for dev, production domain for prod) in SecurityConfig.java

**Checkpoint**: All polish tasks complete, feature ready for production deployment

---

## Dependencies & Parallel Execution

### User Story Dependencies

```
Phase 1 (Setup) 
   ↓
Phase 2 (Foundational) ← MUST COMPLETE BEFORE ANY USER STORY
   ↓
   ├─→ Phase 3 (US1: Student Login) ← MVP START (can run in parallel with US2)
   │     └─→ Phase 5 (US4: Wrappers) ← depends on US1 + US2 foundation
   │
   ├─→ Phase 4 (US2: Parent Access) ← MVP (can run in parallel with US1)
   │     └─→ Phase 5 (US4: Wrappers) ← depends on US1 + US2 foundation
   │
   ├─→ Phase 6 (US5: Homework Sub) ← independent, can run parallel with US3
   │
   ├─→ Phase 7 (US3: Bus Tracking) ← independent, can run parallel with US5
   │
   └─→ Phase 8 (US6: Notifications) ← can start anytime after Phase 2, independent
         │
         └─→ Phase 9 (Notices/Calendar) ← independent
               │
               └─→ Phase 10 (Polish) ← final phase after all user stories
```

### Parallel Execution Examples

**After Phase 2 Complete**:
- **Parallel Stream 1**: T034-T044 (US1 Student Login)
- **Parallel Stream 2**: T045-T057 (US2 Parent Access)
- **Parallel Stream 3**: T071-T082 (US5 Homework Submission)
- **Parallel Stream 4**: T083-T114 (US3 Bus Tracking)
- **Parallel Stream 5**: T115-T132 (US6 Notifications)

**After US1 + US2 Complete**:
- **Parallel Stream 1**: T058-T070 (US4 Wrappers)
- **Parallel Stream 2**: T133-T146 (Notices & Calendar)

**Final Phase (After All User Stories)**:
- **Sequential**: T147-T180 (Polish & Testing)

### Estimated Task Distribution

- **Phase 1 (Setup)**: 8 tasks
- **Phase 2 (Foundational)**: 25 tasks (blocking)
- **Phase 3 (US1)**: 11 tasks (MVP)
- **Phase 4 (US2)**: 13 tasks (MVP)
- **Phase 5 (US4)**: 13 tasks
- **Phase 6 (US5)**: 16 tasks (includes provider abstraction: S3 + Azure + config)
- **Phase 7 (US3)**: 32 tasks (largest - transport system)
- **Phase 8 (US6)**: 18 tasks
- **Phase 9 (Notices/Calendar)**: 14 tasks
- **Phase 10 (Polish)**: 37 tasks (includes provider-specific unit tests)

**Total**: 187 tasks

### MVP Scope (Phases 1-4)

**Deliverable**: Student OTP login + Parent multi-child access + Basic profiles

**Task Count**: 8 (Setup) + 25 (Foundation) + 11 (US1) + 13 (US2) = **57 tasks (32% of total)**

**Value**: Enables mobile app authentication and basic data access for both students and parents

---

## Implementation Strategy

### Week 1: Foundation
- Complete Phase 1 (Setup) + Phase 2 (Foundational)
- Milestone: Database migrations complete, core entities created, JWT auth extended

### Week 2: MVP - Authentication
- Complete Phase 3 (US1) + Phase 4 (US2)
- Milestone: Student and parent OTP login working, dashboard functional

### Week 3: Data Access Layer
- Complete Phase 5 (US4 Wrappers)
- Milestone: All existing APIs wrapped with mobile endpoints and parent authorization

### Week 4: Homework Submission
- Complete Phase 6 (US5)
- Milestone: Students can upload homework submissions, S3 integration working

### Week 5-6: Transport Tracking
- Complete Phase 7 (US3)
- Milestone: Bus live tracking, ETA calculation, GPS ingestion functional

### Week 7: Notifications
- Complete Phase 8 (US6) + Phase 9 (Notices/Calendar)
- Milestone: Push notifications working, notice system complete

### Week 8: Polish & Testing
- Complete Phase 10
- Milestone: All tests passing, OpenAPI docs generated, production-ready

---

## Validation Checklist

### User Story Completeness

- [x] Each user story has independent test criteria
- [x] Each user story maps to entities from data-model.md
- [x] Each user story maps to endpoints from contracts/api-summary.md
- [x] Tasks include exact file paths
- [x] Parallelizable tasks marked with [P]
- [x] Story labels [US1-US6] applied correctly
- [x] Tasks follow checklist format: `- [ ] [ID] [P?] [Story?] Description with path`

### Coverage

- [x] All 18 entities from data-model.md covered
- [x] All 8 migrations (V8-V15) covered
- [x] All authentication endpoints covered
- [x] All user context endpoints covered
- [x] All wrapper endpoints covered
- [x] All transport endpoints covered
- [x] All notification endpoints covered
- [x] Testing tasks included (unit + integration)
- [x] Documentation tasks included (Springdoc annotations + OpenAPI generation)

---

**Tasks Complete**: Ready for implementation. Start with Phase 1 and Phase 2, then proceed to MVP (Phase 3 + Phase 4).
