# Feature Specification: Mobile API Backend Support

**Feature Branch**: `008-mobile-api-support`  
**Created**: 2026-02-13  
**Status**: Draft  
**Input**: User description: "Mobile API backend support for student/parent mobile app including OTP auth, parent model, mobile endpoints, and bus tracking"

## Current Backend State Analysis

### ✅ Already Implemented (No Changes Needed)

- **Student Entity**: Exists with basic fields (name, rollNo, class, guardian info)
- **Attendance APIs**: `/api/attendance` endpoints for history and summary already exist
- **Homework APIs**: `/api/homework` endpoints for create and list already exist  
- **Finance APIs**: `/api/finance` endpoints for ledger, transactions, receipts already exist
- **Timetable/Routine APIs**: `/api/academics/routine` endpoint already exists
- **Results APIs**: `/api/academic/reports/{studentId}` endpoint already exists
- **Auth Framework**: JWT-based authentication with Spring Security already configured
- **RoleNames**: `ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_STUDENT` already exist

### ❌ Missing Features (This Spec's Focus)

Based on the mobile requirements draft analysis, the following critical gaps were identified:

1. **OTP Authentication**: Backend only has email/password auth (`/api/auth/signin`). No OTP request/verify endpoints exist.
2. **ROLE_PARENT**: Not in RoleName enum - causes authorization failures when parents try to access child data
3. **Parent Entity**: Completely missing - no way to represent parent accounts
4. **ParentStudentLink**: Missing - no many-to-many mapping for parent-child relationships
5. **Student.userId**: Missing field - no way to link Student entity to User for authentication
6. **Mobile API Namespace**: No `/api/mobile/v1/*` endpoints exist for mobile-optimized responses
7. **Homework Submission**: No HomeworkSubmission entity or submission endpoints for students
8. **Transport System**: Entire bus tracking infrastructure missing (Vehicle, Route, Stop, Trip, GPS tracking)
9. **Push Notifications**: No notification token registration or FCM/APNs integration
10. **Refresh Token System**: No token rotation or refresh mechanism
11. **OTP Verification Entity**: No temporary OTP storage for verification workflow

**This specification addresses ONLY the missing features above.**

### 🔄 Work Categories

**Category A: Net-New Development (Build from Scratch)**
- OTP authentication flow and endpoints
- Parent entity and ParentStudentLink relationship model
- Homework submission entity and endpoints
- Complete transport/bus tracking system (8 new entities + APIs)
- Push notification infrastructure
- Refresh token mechanism
- Notice system (if not exists - needs verification)

**Category B: Extension/Modification (Enhance Existing)**
- Add ROLE_PARENT to RoleName enum
- Add userId field to Student entity
- Extend PreAuthorize checks to support parent role

**Category C: Wrapper/Delegation (Thin Mobile Layer)**
- Mobile endpoints for attendance (wrap existing `/api/attendance`)
- Mobile endpoints for homework listing (wrap existing `/api/homework`)
- Mobile endpoints for fees (wrap existing `/api/finance`)
- Mobile endpoints for timetable (wrap existing `/api/academics/routine`)
- Mobile endpoints for results (wrap existing `/api/academic/reports`)
- Mobile dashboard aggregation endpoint

**Effort Distribution Estimate:**
- Category A (Net-New): ~70% of development effort
- Category B (Extension): ~10% of development effort  
- Category C (Wrapper): ~20% of development effort

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Student OTP Login and Profile Access (Priority: P1)

A student downloads the mobile app, enters their registered mobile number, receives an OTP, verifies it, and immediately sees their personalized dashboard with attendance summary, pending homework, and upcoming exams.

**Why this priority**: Authentication is the gateway to all mobile features. Without OTP-based mobile login, no student or parent can access the app. This is the most fundamental user journey.

**Independent Test**: Can be fully tested by sending OTP to a registered student mobile number, verifying the OTP, and confirming the session token is returned along with the student's basic profile and role information. Delivers immediate value by enabling mobile access to existing school data.

**Acceptance Scenarios**:

1. **Given** a registered student with mobile number +91-9876543210, **When** they request OTP login, **Then** system generates and sends a 6-digit OTP valid for 5 minutes
2. **Given** a valid OTP is sent, **When** user enters the correct OTP within validity period, **Then** system returns access token, refresh token, and user profile with STUDENT role
3. **Given** an authenticated student session, **When** student calls GET /api/mobile/v1/me, **Then** system returns student profile with associated studentId
4. **Given** an authenticated student session, **When** student calls GET /api/mobile/v1/me/home, **Then** system returns personalized dashboard with today's attendance status, pending homework count, and next exam date

---

### User Story 2 - Parent Multi-Child Account Access (Priority: P1)

A parent logs in using their registered mobile number, verifies OTP, and sees a list of their linked children. They can switch between children to view each child's attendance, homework, results, and fee status independently.

**Why this priority**: Parents are a primary audience for the mobile app and often have multiple children in the same school. Without parent role support and child linking, half the app's target users cannot function.

**Independent Test**: Can be fully tested by creating a parent account linked to two students, authenticating via OTP, calling GET /api/mobile/v1/me/students to retrieve the child list, and then fetching attendance/homework for each child using parent authorization. Delivers full parent visibility into multiple children.

**Acceptance Scenarios**:

1. **Given** a registered parent with mobile number +91-9876543210 linked to two students (IDs 101, 102), **When** parent authenticates via OTP, **Then** system returns tokens and user profile with PARENT role
2. **Given** an authenticated parent session, **When** parent calls GET /api/mobile/v1/me/students, **Then** system returns array of linked children with studentId, name, class, and section for each child
3. **Given** a parent viewing child 101's data, **When** parent requests GET /api/attendance/student/101, **Then** system authorizes based on parent-student linkage and returns child 101's attendance
4. **Given** a parent without linkage to student 201, **When** parent attempts GET /api/attendance/student/201, **Then** system returns 403 Forbidden with error "You are not authorized to view this student's data"

---

### User Story 3 - Bus Live Tracking for Student Route (Priority: P2)

A student assigned to a bus route opens the app in the morning and sees their bus's live location on a map, the estimated arrival time at their stop, and receives a push notification when the bus is 5 minutes away from their pickup point.

**Why this priority**: Bus tracking is a high-value safety and convenience feature for parents and students. It requires complete transport infrastructure (vehicle, route, GPS ingestion, ETA calculation) but is not blocking for core academic features.

**Independent Test**: Can be fully tested independently by creating a bus route, assigning a student to it, simulating GPS location pings from a tracking device, and verifying that GET /api/mobile/v1/transport/live returns current location and ETA. Push notification can be tested by triggering proximity events. Delivers standalone value as a safety monitoring tool.

**Acceptance Scenarios**:

1. **Given** a student assigned to bus route R-101, **When** student calls GET /api/mobile/v1/me/transport, **Then** system returns route details with bus number, driver contact, and assigned stops
2. **Given** a GPS device sending location pings for vehicle V-42 on route R-101, **When** student calls GET /api/mobile/v1/transport/live?routeId=R-101, **Then** system returns current latitude, longitude, speed, heading, and timestamp of last ping (within last 30 seconds for real-time status)
3. **Given** a student's pickup stop is 2km away and bus is approaching at 30 km/h, **When** student calls GET /api/mobile/v1/transport/stops?routeId=R-101, **Then** system returns ordered stop list with ETA indicating "5 minutes" for student's stop
4. **Given** no GPS ping received for 2 minutes, **When** student calls GET /api/mobile/v1/transport/live, **Then** system returns stale=true flag with last known location and timestamp indicating "Last updated 2 minutes ago"

---

### User Story 4 - Mobile-Optimized Dashboard Context (Priority: P2)

A student logs in via OTP and calls a single mobile dashboard endpoint that returns everything needed for the home screen: today's attendance status, pending homework count, next exam, and pending fees in one optimized response instead of making 5+ separate API calls.

**Why this priority**: Existing APIs work but are not optimized for mobile bandwidth and latency constraints. Mobile apps need aggregated context endpoints to reduce round trips and improve TTI (Time To Interactive).

**Independent Test**: Can be fully tested by authenticating as a student and calling GET /api/mobile/v1/me/home, which returns aggregated dashboard data from multiple backend services in a single response. Delivers value by improving mobile app performance.

**Acceptance Scenarios**:

1. **Given** an authenticated student 101 with today's attendance marked Present, 2 pending homework assignments, next exam on 2026-02-20, and ₹5000 pending fees, **When** student calls GET /api/mobile/v1/me/home, **Then** system returns aggregated response with attendanceToday=Present, pendingHomeworkCount=2, nextExam={date, subject}, pendingFees=5000
2. **Given** an authenticated parent linked to 2 children, **When** parent calls GET /api/mobile/v1/me/home?studentId=101, **Then** system validates parent-student linkage and returns child 101's aggregated dashboard data
3. **Given** a parent without linkage to student 201, **When** parent calls GET /api/mobile/v1/me/home?studentId=201, **Then** system returns 403 Forbidden
4. **Given** backend services are slow, **When** mobile app calls /me/home with timeout=3000ms, **Then** system returns partial data with status flags for timed-out services rather than failing completely

---

### User Story 5 - Homework Submission and Completion Tracking (Priority: P2)

A student views assigned homework for today's classes, uploads a completed assignment (image or PDF), marks it as submitted, and the teacher later sees the submitted work. Parents can view homework status for their children but cannot submit on behalf of students.

**Why this priority**: Homework workflow is a daily interaction point between students and teachers. Submission capability enhances the mobile app's utility beyond read-only access and adds accountability.

**Independent Test**: Can be fully tested by creating a homework assignment for a class, authenticating as a student in that class, calling POST /api/mobile/v1/homework/{hwId}/submit with file upload, and verifying submission is recorded and visible to teacher. Parent read-only access can be tested independently. Delivers value by digitizing homework submission.

**Acceptance Scenarios**:

1. **Given** a student in Class 10-A, **When** student calls GET /api/mobile/v1/homework?classId=10A&date=2026-02-13, **Then** system returns list of homework assignments with subject, description, dueDate, and submissionStatus
2. **Given** an unsubmitted homework with ID HW-501, **When** student uploads a file and calls POST /api/mobile/v1/homework/HW-501/submit, **Then** system accepts submission, stores file reference, timestamps the submission, and updates homework status to "Submitted"
3. **Given** a parent linked to student 101 in Class 10-A, **When** parent calls GET /api/mobile/v1/homework?studentId=101, **Then** system returns homework list for student 101 with read-only access (no submission endpoint accessible)
4. **Given** homework HW-501 submitted by student 101 at 2026-02-13 10:30 AM, **When** teacher views homework submission list, **Then** teacher sees student 101's submission with timestamp and file attachment

---

### User Story 6 - Push Notifications for Critical Events (Priority: P3)

A parent receives a push notification on their phone when their child is marked absent, when a new school notice is published, when fees are due in 3 days, or when the school bus is approaching the pickup stop. Students also receive notifications for new homework assignments and exam announcements.

**Why this priority**: Notifications improve engagement and reduce missed information, but core features (auth, data access, payment) can function without push. This is an enhancement layer.

**Independent Test**: Can be fully tested independently by registering a device FCM token via POST /api/mobile/v1/notifications/register, triggering an absence event for a student, and verifying that the backend sends a push notification payload to FCM/APNs. Delivered notifications can be observed in device notification tray. Delivers proactive alerts without requiring app to be open.

**Acceptance Scenarios**:

1. **Given** a parent's device with FCM token "abc123xyz", **When** parent calls POST /api/mobile/v1/notifications/register with token and userId, **Then** system stores token linked to parent account
2. **Given** a student is marked absent on 2026-02-13, **When** absence event is recorded, **Then** system triggers push notification to parent's registered device with message "Your child [Name] was marked absent today"
3. **Given** a new school-wide notice is published, **When** notice is created with priority=HIGH, **Then** system sends push notification to all parent and student devices with message "New Notice: [Title]"
4. **Given** fees are due in 3 days for student 101, **When** automated daily job runs, **Then** system sends reminder push notification to parent linked to student 101 with message "Fee payment due in 3 days: ₹5000"

---

### Edge Cases

- **What happens when a parent is linked to a student but the student's user account is also active?** The student and parent should both be able to view the student's data independently. Authorization must validate that either the session belongs to the student OR the session belongs to a parent linked to that student.
  
- **How does system handle OTP requests for a mobile number with multiple accounts (e.g., one student and one parent account)?** System should identify all accounts linked to the mobile number and either (a) send OTP to all accounts with role selection after verification, or (b) prompt user to specify role before sending OTP.

- **What if GPS location data for a bus becomes stale mid-trip (e.g., device offline for 10 minutes)?** System must return `stale=true` flag and display "Last updated X minutes ago" in the mobile UI. ETA calculations should be marked as unreliable or switch to scheduled time estimates.

- **What happens when a payment gateway transaction succeeds but webhook confirmation fails?** System must support manual transaction reconciliation via idempotency key and allow manual verification workflows to prevent double-charging or lost payments.

- **How does system prevent parent from accessing data for a child after parent-student linkage is removed by admin?** Linkage validation must occur on every request—not just at login. If linkage is revoked mid-session, next data request should return 403 Forbidden even if access token is still valid.

- **What if a student submits homework after the due date?** System should accept late submissions but mark them with a "Late" status and timestamp. Business rules about accepting/rejecting late submissions should be configurable per homework assignment.

- **How does system handle concurrent OTP requests from same mobile number?** Only the most recent OTP should be valid. Previous OTPs should be invalidated immediately when a new OTP is requested for the same number.

- **What if a parent has no linked children at login time?** System should return empty array for GET /api/mobile/v1/me/students and display a message in mobile UI instructing parent to contact school office to link their child.

- **How are parent-student linkages created initially?** This is an administrative workflow (outside mobile app scope). School admin must create parent accounts and establish linkages via web admin panel. Mobile app only consumes these linkages.

- **What happens if the same homework assignment is submitted multiple times by a student?** System should support resubmission (overwrite previous submission with new timestamp) unless teacher has locked/graded the homework. Latest submission should always be considered the active one.

## Requirements *(mandatory)*

### Functional Requirements

#### Authentication & Session Management

- **FR-001**: System MUST provide OTP-based authentication endpoints for mobile login (POST /api/mobile/v1/auth/request-otp and POST /api/mobile/v1/auth/verify-otp)
- **FR-002**: System MUST generate 6-digit numeric OTP valid for 5 minutes per mobile number request
- **FR-003**: System MUST support only one active OTP per mobile number (new OTP request invalidates previous OTP)
- **FR-004**: System MUST return both access token (short-lived, 15 minutes) and refresh token (long-lived, 30 days) upon successful OTP verification
- **FR-005**: System MUST implement token refresh endpoint (POST /api/mobile/v1/auth/refresh) that accepts refresh token and returns new access token
- **FR-006**: System MUST invalidate refresh token after use (rotation policy) and issue a new refresh token with each refresh operation
- **FR-007**: System MUST support logout endpoint (POST /api/mobile/v1/auth/logout) that invalidates current refresh token
- **FR-008**: System MUST validate access tokens on every protected mobile endpoint request using JWT verification

#### Role Management & Authorization

- **FR-009**: System MUST add PARENT role to the existing role enum alongside ADMIN, TEACHER, STUDENT
- **FR-010**: System MUST ensure consistent role checking across all controllers using standardized PreAuthorize annotations
- **FR-011**: System MUST support role-based access control where STUDENT can access only own data, PARENT can access linked children's data, and TEACHER/ADMIN have broader access per existing policies
- **FR-012**: System MUST validate parent-student linkage on every request where parent accesses child data (never trust client-submitted studentId without server-side linkage check)

#### Parent Domain Model

- **FR-013**: System MUST create Parent entity with fields: id, userId (FK to User), firstName, lastName, mobileNumber, email (optional), createdAt, updatedAt
- **FR-014**: System MUST create ParentStudentLink entity representing many-to-many relationship with fields: id, parentId (FK to Parent), studentId (FK to Student), relationshipType (e.g., "Father", "Mother", "Guardian"), isPrimary (boolean), createdAt
- **FR-015**: System MUST support multiple parents linked to one student and multiple students linked to one parent
- **FR-016**: System MUST provide admin APIs to create parent accounts and manage parent-student linkages (out of scope for mobile app; admin web panel only)

#### Student-User Linkage

- **FR-017**: System MUST add userId field (FK to User) to Student entity to enable direct student login and "my data" resolution
- **FR-018**: System MUST ensure one-to-one relationship between Student and User for student role accounts
- **FR-019**: System MUST populate studentId in JWT claims when user with STUDENT role authenticates

#### Mobile API Endpoints - User Context

- **FR-020**: System MUST provide GET /api/mobile/v1/me endpoint that returns current authenticated user's profile including userId, name, role, and associated studentId (if STUDENT) or parentId (if PARENT)
- **FR-021**: System MUST provide GET /api/mobile/v1/me/students endpoint for PARENT role that returns array of linked children with studentId, name, class, section, rollNumber, and photo URL
- **FR-022**: System MUST provide GET /api/mobile/v1/me/home endpoint that returns personalized dashboard data including: today's attendance status, pending homework count, upcoming exam (next 7 days), pending fee amount, and recent notices (max 5)
- **FR-023**: All /api/mobile/v1/* endpoints MUST follow versioned namespace convention to enable future API evolution without breaking changes

#### Mobile API Endpoints - Attendance (Wrapper Layer Only)

- **FR-024**: System MUST provide GET /api/mobile/v1/attendance/summary endpoint that wraps existing `/api/attendance/summary` with mobile response format and parent-student linkage validation
- **FR-025**: System MUST provide GET /api/mobile/v1/attendance/history endpoint that wraps existing `/api/attendance` endpoint with mobile response format and authorization
- **FR-026**: Mobile attendance wrapper MUST resolve studentId from JWT for STUDENT role (auto-inject current user's studentId) and validate parent-student linkage for PARENT role before delegating to existing attendance service

#### Mobile API Endpoints - Homework (New Submission Feature)

- **FR-027**: System MUST provide GET /api/mobile/v1/homework endpoint that wraps existing `/api/homework` with mobile response format including submissionStatus per student
- **FR-028**: System MUST create **NEW** HomeworkSubmission entity with fields: id, homeworkId (FK), studentId (FK), submittedAt, submissionFileUrls (array), status (submitted/late/reviewed), grade, teacherRemarks
- **FR-029**: System MUST provide **NEW** POST /api/mobile/v1/homework/{id}/submit endpoint for STUDENT role that accepts multipart file upload, stores file in cloud storage, and creates HomeworkSubmission record
- **FR-030**: System MUST support homework resubmission by updating existing HomeworkSubmission record (keep submission history with previous file references)
- **FR-031**: System MUST provide GET /api/mobile/v1/homework/{id}/submission endpoint that returns student's submission details if exists

#### Mobile API Endpoints - Timetable (Wrapper Only)

- **FR-032**: System MUST provide GET /api/mobile/v1/timetable endpoint that wraps existing `/api/academics/routine` with mobile response format and auto-resolves classId from studentId
- **FR-033**: Timetable endpoint MUST authorize: STUDENT auto-resolves to own class, PARENT validates linkage before accessing child's class routine

#### Mobile API Endpoints - Results (Wrapper Only)

- **FR-034**: System MUST provide GET /api/mobile/v1/results endpoint that wraps existing `/api/academic/reports/{studentId}` with mobile response format
- **FR-035**: Results endpoint MUST authorize: STUDENT auto-resolves to own studentId, PARENT validates linkage before accessing child's results
- **FR-036**: PDF report card generation (if needed) can be added in future phase - existing JSON response sufficient for MVP

#### Mobile API Endpoints - Fees (Wrapper Only)

- **FR-038**: System MUST provide GET /api/mobile/v1/fees/ledger endpoint that wraps existing `/api/finance/students/{id}/ledger` with mobile response format
- **FR-039**: System MUST provide POST /api/mobile/v1/fees/pay endpoint that wraps existing `/api/finance/transactions` with parent-student linkage validation
- **FR-040**: System MUST provide GET /api/mobile/v1/fees/receipt/{receiptId} endpoint that wraps existing `/api/finance/receipts/{receiptNo}` with mobile response format
- **FR-041**: Fee endpoints MUST authorize: both STUDENT (auto-resolve) and PARENT (validate linkage) can view fees and make payments for authorized student accounts
- **FR-042**: Payment idempotency is assumed to be handled by existing finance transaction service

#### Mobile API Endpoints - Notices (New Feature if Backend Missing)

- **FR-044**: System MUST create Notice entity if not exists with fields: id, title, description, publishDate, priority, targetRole, targetClassIds, attachmentUrls, createdBy
- **FR-045**: System MUST create NoticeReadStatus entity with userId, noticeId, readAt to track read status
- **FR-046**: System MUST provide GET /api/mobile/v1/notices endpoint that returns notices filtered by user role and linked student classes for parents
- **FR-047**: System MUST provide GET /api/mobile/v1/notices/{id} endpoint that marks notice as read and returns full details

**Note**: If Notice entity already exists in backend, this becomes a wrapper layer similar to other endpoints.

#### Mobile API Endpoints - Calendar (Aggregation Layer)

- **FR-048**: System MUST provide GET /api/mobile/v1/calendar endpoint that aggregates events from multiple sources: exam schedules (from existing exam entity), holidays (from holiday entity if exists), and notice dates
- **FR-049**: Calendar endpoint MUST authorize: STUDENT sees own class events, PARENT sees events for all linked children aggregated

**Note**: This is primarily an aggregation/transformation layer over existing academic data, not new entities.

#### Transport/Bus Tracking System

- **FR-049**: System MUST create Vehicle entity with fields: id, vehicleNumber, vehicleType, capacity, driverName, driverMobile, gpsDeviceId, isActive
- **FR-050**: System MUST create Route entity with fields: id, routeName, routeNumber, startTime, endTime, vehicleId (FK), isActive
- **FR-051**: System MUST create Stop entity with fields: id, routeId (FK), stopName, stopOrder (sequence), latitude, longitude, scheduledArrivalTime
- **FR-052**: System MUST create Trip entity with fields: id, routeId (FK), vehicleId (FK), tripDate, tripType (morning/afternoon), status (scheduled/in-progress/completed), startTime, endTime
- **FR-053**: System MUST create StudentTransportAssignment entity with fields: id, studentId (FK), routeId (FK), pickupStopId (FK), dropStopId (FK), effectiveFrom, effectiveTo, isActive
- **FR-054**: System MUST create LocationPing entity with fields: id, vehicleId (FK), tripId (FK optional), latitude, longitude, speed, heading, accuracy, capturedAt (device timestamp), receivedAt (server timestamp)
- **FR-055**: System MUST create TransportEvent entity with fields: id, tripId (FK), studentId (FK optional), eventType (bus_start/stop_arrival/student_pickup/student_drop/trip_complete), eventTime, latitude, longitude, remarks

#### Mobile API Endpoints - Transport (Live Tracking)

- **FR-056**: System MUST provide GET /api/mobile/v1/transport/my-route endpoint for STUDENT/PARENT that returns assigned route details with vehicleNumber, driverName, driverMobile, pickupStop, dropStop, and scheduledTimes
- **FR-057**: System MUST provide GET /api/mobile/v1/transport/live endpoint with param routeId that returns current bus location: latitude, longitude, speed, heading, lastUpdatedAt, and stale flag (true if no ping in last 2 minutes)
- **FR-058**: System MUST provide GET /api/mobile/v1/transport/stops endpoint with param routeId that returns ordered stop list with stopName, scheduledTime, status (pending/approaching/completed), and ETA in minutes
- **FR-059**: System MUST provide GET /api/mobile/v1/transport/events endpoint with params studentId, date that returns pickup/drop event history for specified date
- **FR-060**: System MUST calculate ETA for each stop based on current bus location, speed, route distance, and historical speed data (or fallback to scheduled time if GPS data is stale)
- **FR-061**: Transport endpoints MUST authorize: STUDENT can access only assigned route, PARENT can access routes for linked children

#### Transport API Endpoints - GPS Ingestion (Device/Provider Side)

- **FR-062**: System MUST provide POST /api/mobile/v1/transport/location-pings endpoint for GPS device/provider integration that accepts vehicleId, latitude, longitude, speed, heading, accuracy, capturedAt and stores LocationPing record
- **FR-063**: System MUST provide POST /api/mobile/v1/transport/events endpoint for manual/device event logging that accepts tripId, studentId, eventType, eventTime, latitude, longitude, remarks and stores TransportEvent record
- **FR-064**: GPS ingestion endpoints MUST authenticate using device-specific API keys (not user JWT tokens)

#### Push Notifications

- **FR-065**: System MUST provide POST /api/mobile/v1/notifications/register endpoint that accepts FCM/APNs device token and associates it with authenticated user account
- **FR-066**: System MUST provide POST /api/mobile/v1/notifications/unregister endpoint to remove device token when user logs out or uninstalls app
- **FR-067**: System MUST support sending push notifications for critical events: student absence, fee due reminder (3 days before), new high-priority notice, exam announcement, and bus arrival alert (when bus is within 2km of student's pickup stop)
- **FR-068**: Push notification service MUST integrate with Firebase Cloud Messaging (FCM) for Android and Apple Push Notification Service (APNs) for iOS
- **FR-069**: System MUST store notification history in NotificationLog entity with userId, notificationType, title, body, sentAt, deliveryStatus

#### API Contract & Response Standards

- **FR-070**: All mobile API endpoints under /api/mobile/v1 MUST return standardized response envelope with fields: success (boolean), data (object/array/null), error (object with code, message, details or null), meta (object with requestId, timestamp, pagination if applicable)
- **FR-071**: System MUST use consistent HTTP status codes: 200 OK (success), 400 Bad Request (validation error), 401 Unauthorized (missing/invalid token), 403 Forbidden (insufficient permissions), 404 Not Found, 500 Internal Server Error
- **FR-072**: System MUST include correlation/request ID in all API responses for traceability
- **FR-073**: System MUST version all mobile APIs under /api/mobile/v1 namespace and maintain backward compatibility within v1 (breaking changes require v2)
- **FR-074**: System MUST document all mobile API endpoints in OpenAPI 3.0 specification before implementation

#### Error Handling & Logging

- **FR-075**: System MUST return user-friendly error messages for mobile clients (no stack traces or internal system details in production)
- **FR-076**: System MUST log all authentication attempts (success and failure) with userId, mobile number, timestamp, IP address, and user agent
- **FR-077**: System MUST log all financial transactions (fee payments) with full audit trail including userId, studentId, amount, transactionId, paymentMethod, status, timestamp
- **FR-078**: System MUST log all parent-student data access requests for compliance and audit purposes

#### Security & Privacy

- **FR-079**: System MUST encrypt sensitive data at rest including user passwords (hashed only), OTPs (encrypted), refresh tokens (hashed), and payment transaction details
- **FR-080**: System MUST implement rate limiting on OTP request endpoint: maximum 3 OTP requests per mobile number per 15-minute window
- **FR-081**: System MUST implement rate limiting on OTP verification endpoint: maximum 5 failed attempts per mobile number per 15-minute window before temporary account lock
- **FR-082**: System MUST never expose PII (mobile numbers, addresses, parent details) in logs or error messages
- **FR-083**: System MUST restrict bus live location access to authorized users only (student assigned to route or parent of assigned student)
- **FR-084**: System MUST retain GPS location pings for maximum 30 days for privacy compliance (configurable retention policy)

#### Data Synchronization & Caching

- **FR-085**: System MUST support efficient data pagination for large lists (homework, notices, attendance history) with pageSize and pageNumber params
- **FR-086**: System MUST include Last-Modified and ETag headers in cacheable responses to enable client-side conditional requests
- **FR-087**: System MUST support delta sync for attendance and homework updates: mobile clients can request "changes since last sync timestamp"

### Key Entities

**New Entities to Create:**

- **Parent**: NEW entity with id, userId (FK to User), firstName, lastName, mobileNumber, email (optional), address, occupation, createdAt, updatedAt. Links to Students via ParentStudentLink for multi-child access.

- **ParentStudentLink**: NEW entity representing parent-student relationship with id, parentId (FK to Parent), studentId (FK to Student), relationshipType (Father/Mother/Guardian/Other), isPrimary (boolean), createdAt. Enables many-to-many relationship and authorization checks.

- **OtpVerification**: NEW entity for OTP workflow with id, mobileNumber, otpCode (encrypted), purpose (login/password-reset), expiresAt, isUsed, createdAt. Automatically cleaned up after expiration or use.

- **RefreshToken**: NEW entity to track active refresh tokens with id, userId (FK), tokenHash (hashed refresh token), deviceId (optional), expiresAt, isRevoked, createdAt. Enables token rotation and remote logout.

- **HomeworkSubmission**: NEW entity for student homework submissions with id, homeworkId (FK), studentId (FK), submittedAt, submissionFileUrls (array), remarks, status (submitted/late/reviewed), grade (optional), teacherRemarks (optional). Tracks submission history.

- **Vehicle**: NEW entity for transport with id, vehicleNumber, vehicleType (Bus/Van/Car), capacity, driverName, driverMobile, driverLicense, gpsDeviceId, insuranceExpiry, lastMaintenanceDate, isActive.

- **Route**: NEW entity with id, routeName, routeNumber, vehicleId (FK to Vehicle), startTime, endTime, totalDistance (km), estimatedDuration (minutes), isActive.

- **Stop**: NEW entity with id, routeId (FK), stopName, stopOrder (sequence), latitude, longitude, scheduledArrivalTime, landmarkDescription. Ordered list defines route path.

- **Trip**: NEW entity for bus journey with id, routeId (FK), vehicleId (FK), tripDate, tripType (morning_pickup/afternoon_drop), status (scheduled/in-progress/completed/cancelled), startTime, endTime.

- **StudentTransportAssignment**: NEW entity mapping students to routes with id, studentId (FK), routeId (FK), pickupStopId (FK to Stop), dropStopId (FK to Stop), effectiveFrom, effectiveTo (optional), isActive.

- **LocationPing**: NEW entity for GPS tracking with id, vehicleId (FK), tripId (FK optional), latitude, longitude, speed (km/h), heading (degrees), accuracy (meters), capturedAt (device timestamp), receivedAt (server timestamp). High-frequency data.

- **TransportEvent**: NEW entity for significant transport events with id, tripId (FK), studentId (FK optional), eventType (bus_start/stop_arrival/student_pickup/student_drop/emergency_stop/trip_complete), eventTime, latitude, longitude, remarks.

- **NotificationToken**: NEW entity for push with id, userId (FK), token (FCM/APNs token string), platform (android/ios), deviceId, registeredAt, lastUsedAt, isActive. Supports multiple devices per user.

- **NotificationLog**: NEW entity for audit with id, userId (FK), notificationType (absence/fee_due/notice/exam/bus_alert), title, body, sentAt, deliveryStatus (pending/sent/delivered/failed), readAt (optional).

- **Notice** (if not exists): Entity with id, title, description, publishDate, priority, targetRole, targetClassIds, attachmentUrls, createdBy. Check if exists before creating.

- **NoticeReadStatus** (if not exists): Entity with id, userId, noticeId, readAt to track read receipts.

**Entities to Modify:**

- **Student**: ADD userId field (FK to User, nullable for legacy data) to enable direct student login and "my data" resolution. One-to-one relationship with User for student role accounts.

- **RoleName** (enum): ADD ROLE_PARENT value to support parent role authorization.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Mobile OTP authentication completes end-to-end (request OTP → receive OTP → verify OTP → receive tokens) in under 10 seconds for 95th percentile requests
- **SC-002**: Parent with 3 linked children can switch between children and view attendance for each child within 2 seconds per switch
- **SC-003**: Student can view homework list for current week and submit an assignment (with image upload up to 5MB) within 30 seconds total
- **SC-004**: Fee payment flow (view ledger → initiate payment → receive confirmation → download receipt) completes within 45 seconds including payment gateway processing
- **SC-005**: Bus live tracking returns location data with latency under 5 seconds from GPS ping receipt to mobile API response
- **SC-006**: Push notification delivery succeeds for 95% of registered active devices within 30 seconds of event trigger
- **SC-007**: System handles 1000 concurrent mobile API requests without response time degradation beyond 10% of baseline
- **SC-008**: Parent can access homework, attendance, results, and fees for any linked child without encountering authorization errors (assuming valid linkage exists)
- **SC-009**: All mobile API endpoints return standardized error responses with actionable error codes and user-friendly messages
- **SC-010**: Zero unauthorized data access incidents where parent views data for non-linked student or student views another student's data
- **SC-011**: GPS location data staleness is detected and flagged within 2 minutes of last ping, preventing inaccurate ETA display
- **SC-012**: Rate limiting prevents abuse: no more than 3 OTP requests allowed per mobile number per 15-minute window
- **SC-013**: Mobile API documentation (OpenAPI spec) covers 100% of mobile endpoints with request/response examples and error codes
- **SC-014**: Backend supports minimum 500 active bus routes with live GPS tracking without performance degradation
- **SC-015**: Homework submission files up to 10MB upload successfully with progress tracking and resume capability on network interruption

## Assumptions

- **Assumption 1**: SMS gateway integration for OTP delivery is out of scope for this backend feature. Backend will generate OTP and expose it via response/logs for testing. Mobile team or separate service will handle actual SMS delivery integration.

- **Assumption 2**: Payment gateway integration (Razorpay/PayU/etc.) already exists in `/api/finance/transactions` endpoint. Mobile wrapper will reuse existing payment logic with added parent-student linkage validation.

- **Assumption 3**: School admin web panel already exists for managing parent accounts and parent-student linkages. Mobile backend assumes these linkages are created by admins and only exposes read-only linkage data to mobile APIs.

- **Assumption 4**: GPS tracking devices or provider APIs are already deployed on buses and sending location pings. Backend will expose ingestion endpoint but device/provider integration is separate scope.

- **Assumption 5**: File storage service (for homework submissions, receipts, notice attachments) already exists or will use cloud storage (S3/GCS). Backend will store file URLs/references only, not handle file storage infrastructure.

- **Assumption 6**: Existing User, Student, Class, Subject, and Attendance entities are already defined in the database. This feature extends those models (adds userId to Student, adds Parent entity) but does not create core academic structure from scratch.

- **Assumption 7**: Teacher and Admin roles can already create homework, attendance, results, and notices via existing web interface (confirmed by existing controllers). Mobile backend only exposes these for read access and adds student homework submission capability.

- **Assumption 8**: Email notifications are out of scope. Push notifications via FCM/APNs are in scope. Email reminders (if needed) will be separate feature.

- **Assumption 9**: Multi-language support (i18n) is handled by mobile app. Backend will return content in school's primary language (likely English/Hindi). Localization of API response text is not in backend scope.

- **Assumption 10**: Existing Spring Security configuration is in place (confirmed by AuthController). This feature extends it with OTP-based mobile auth and refresh tokens but does not replace existing session-based web authentication.

- **Assumption 11**: Existing endpoints (`/api/attendance`, `/api/homework`, `/api/finance`, `/api/academics/routine`, `/api/academic/reports`) will remain unchanged. Mobile APIs will wrap/delegate to these services with added authorization logic for parent role.

- **Assumption 12**: Database migration tool (Flyway/Liquibase) is already configured. Migration scripts for new entities will be added to existing migration workflow.

## Dependencies

- **Dependency 1**: SMS gateway service or provider API must be available to send OTP messages to mobile numbers. Backend can generate OTPs but cannot deliver them without external SMS service.

- **Dependency 2**: Firebase Cloud Messaging (FCM) project and APNs certificates must be configured for push notification delivery. Backend will send notification payloads but FCM/APNs infrastructure is external dependency.

- **Dependency 3**: Payment gateway merchant account and API credentials required for fee payment integration (Razorpay, PhonePe, PayU, or similar). Backend will integrate with gateway APIs once credentials are provided.

- **Dependency 4**: GPS tracking device provider or IoT platform must expose API or webhook for location ping ingestion. Backend defines ingestion endpoint but depends on device/provider sending data.

- **Dependency 5**: Cloud storage service (AWS S3, Google Cloud Storage, or Azure Blob) required for storing homework attachments, receipts, and notice documents. Backend will use cloud SDK to generate signed URLs.

- **Dependency 6**: Database migration tool (Flyway/Liquibase) must be configured to execute schema migrations for new entities (Parent, ParentStudentLink, Vehicle, Route, Stop, etc.). Migration scripts must be reviewed by DBA before production deployment.

- **Dependency 7**: Reverse geocoding service (Google Maps API, Mapbox, or OpenStreetMap) optional but recommended for converting GPS coordinates to readable addresses for stop names and location display.

- **Dependency 8**: Mobile app (gurukul) must be developed in parallel to consume these APIs. Backend APIs are useless without frontend consumer, so mobile team coordination is critical dependency.

- **Dependency 9**: School admin team must populate parent accounts, parent-student linkages, transport routes, vehicle data, and student transport assignments before mobile app can show meaningful data.

- **Dependency 10**: Load balancing and auto-scaling infrastructure must be provisioned to handle concurrent mobile requests during peak hours (morning bus tracking, exam result announcements, fee payment deadlines).

## Out of Scope

- **Rebuilding existing APIs**: Attendance, homework listing, finance ledger, timetable, and results APIs already exist. This feature wraps them with mobile-optimized responses and parent authorization—NOT rebuilding from scratch.

- **Web frontend changes**: This feature focuses on backend API development for mobile app. Any changes to existing school admin or teacher web interfaces are out of scope. Web frontend will continue using existing endpoints.

- **Admin panel for parent management**: While backend will create Parent entity and linkage tables, the admin UI for managing parents, creating linkages, and assigning transport is out of scope. This is separate feature for school-operations-dashboard.

- **Teacher mobile app**: This feature supports only Student and Parent mobile users. Teacher-specific mobile features (attendance marking, homework grading from mobile) are future scope.

- **Offline-first mobile app**: Backend provides standard REST APIs. Advanced offline sync, conflict resolution, and offline mutation queue are mobile app concerns, not backend scope.

- **Real-time websocket notifications**: Push notifications via FCM/APNs are in scope. Real-time websocket connections for live data streaming are out of scope for MVP.

- **Advanced analytics and reports**: Parent can view basic attendance summary and results. Advanced analytics like attendance trends, performance charts, comparative analysis are out of scope for this feature.

- **In-app chat/messaging**: Communication between parents and teachers via in-app chat is out of scope. Notices are one-way broadcast only.

- **Online exam module**: Results viewing is in scope. Conducting online exams via mobile app is out of scope.

- **Library management**: Book issue/return tracking for students is out of scope.

- **Canteen/meal management**: Meal ordering or balance tracking is out of scope.

- **Event RSVP and consent forms**: Calendar view of events is in scope. RSVP, permission slips, and consent form signing are out of scope.

- **Gradebook and continuous assessment**: Viewing final term results is in scope. Detailed gradebook with assignment-level scores and formative assessment tracking is out of scope.

- **Bus route optimization**: Backend stores static routes. Dynamic route optimization based on traffic, student locations, or driver availability is out of scope.

- **Emergency alert system**: General push notifications are in scope. Panic button, emergency broadcasting, or SOS features are out of scope for MVP.

- **Multi-school/multi-tenant support**: This backend assumes single school deployment. Multi-tenant architecture with school-specific branding and data isolation is future scope.

- **Third-party integrations**: Learning management systems (LMS), video conferencing, digital library integrations are out of scope.

- **Biometric attendance**: Mobile app shows attendance records. Biometric device integration for marking attendance is separate scope.

- **Fee installment plans**: Simple fee payment is in scope. Complex installment plans, discounts, scholarships, and payment plans managed via mobile are out of scope.

- **Report card customization**: System returns report card data. Custom report card templates, school logo placement, and design customization are out of scope.

- **Modifying existing endpoint paths**: Current endpoints like `/api/attendance`, `/api/homework`, `/api/finance` remain unchanged. Mobile APIs live under new `/api/mobile/v1` namespace only.
