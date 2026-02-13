# Phase 1: Data Model & Entity Relationships

**Feature**: Mobile API Backend Support  
**Date**: 2026-02-13  
**Purpose**: Define database schema, entity relationships, and domain model structure

## Entity Relationship Overview

```
┌──────────┐         ┌─────────┐         ┌──────────────┐
│   User   │◄───────►│ Student │◄───────►│ ParentStudent│
│          │ 1     1 │         │ *     * │   Link       │
└──────────┘         └─────────┘         └──────────────┘
     │ 1                   │ 1                   │ *
     │                     │                     │
     │ *                   │ *                   │ 1
┌──────────┐         ┌──────────────────┐  ┌──────────┐
│  Parent  │         │StudentTransport  │  │  Parent  │
│          │         │  Assignment      │  │          │
└──────────┘         └──────────────────┘  └──────────┘
     │ 1                   │ *
     │                     │
     │ *                   │ 1
┌──────────────┐     ┌──────────┐
│RefreshToken  │     │  Route   │
│              │     │          │
└──────────────┘     └──────────┘
                          │ 1
                          │
                          │ *
                     ┌──────────┐         ┌──────────┐
                     │   Stop   │         │ Vehicle  │
                     │          │         │          │
                     └──────────┘         └──────────┘
                                               │ 1
                                               │
                                               │ *
                                          ┌──────────────┐
                                          │ LocationPing │
                                          │              │
                                          └──────────────┘
```

---

## Core Entities

### 1. User (Modified)

**Purpose**: Authentication principal for all system users

**Existing Fields**:
- `id` (BIGINT, PK)
- `username` (VARCHAR, unique)
- `email` (VARCHAR, unique)
- `password` (VARCHAR, BCrypt hashed)
- `role` (ENUM: ROLE_USER, ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT, **ROLE_PARENT** ← NEW)
- `is_active` (BOOLEAN
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Modification Required**:
- Add `ROLE_PARENT` to role enum
- Optionally add `mobile_number` (VARCHAR, unique) if not exists for OTP login

**Relationships**:
- 1:1 with Student (via Student.user_id)
- 1:1 with Parent (via Parent.user_id)
- 1:* with RefreshToken
- 1:* with OtpVerification

---

### 2. Student (Modified)

**Purpose**: Student academic information

**Existing Fields**:
- `id` (BIGINT, PK)
- `first_name` (VARCHAR)
- `last_name` (VARCHAR)
- `name` (VARCHAR) - legacy, synced with first+last
- `roll_no` (INTEGER)
- `class_id` (BIGINT, FK → Class)
- `gender` (VARCHAR)
- `guardian_name` (VARCHAR) - legacy field
- `guardian_mobile` (VARCHAR) - legacy field
- `deleted` (BOOLEAN)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Modification Required**:
- Add `user_id` (BIGINT, FK → User, nullable, unique) ← **NEW FIELD**
  - Nullable to support legacy student records without user accounts
  - Unique constraint ensures 1:1 relationship when present

**Relationships**:
- *:1 with Class (existing)
- 1:1 with User (new, via user_id)
- *:* with Parent (via ParentStudentLink)
- 1:* with HomeworkSubmission
- 1:1 with StudentTransportAssignment

**Migration Consideration**: 
```sql
ALTER TABLE students ADD COLUMN user_id BIGINT NULL;
ALTER TABLE students ADD CONSTRAINT fk_student_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
CREATE UNIQUE INDEX idx_student_user_id ON students(user_id) WHERE user_id IS NOT NULL;
```

---

## New Entities

### 3. Parent

**Purpose**: Parent/guardian information for mobile app access

**Table**: `parents`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| user_id | BIGINT | FK → users(id), NOT NULL, UNIQUE | Links to authentication account |
| first_name | VARCHAR(50) | NOT NULL | Parent's first name |
| last_name | VARCHAR(50) | NULL | Parent's last name |
| mobile_number | VARCHAR(15) | NOT NULL, UNIQUE | Primary mobile for OTP login |
| email | VARCHAR(100) | NULL | Optional email |
| address | TEXT | NULL | Residential address |
| occupation | VARCHAR(100) | NULL | Professional occupation |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Indexes**:
- PRIMARY KEY (id)
- UNIQUE INDEX idx_parent_user_id (user_id)
- UNIQUE INDEX idx_parent_mobile (mobile_number)

**Relationships**:
- 1:1 with User (via user_id)
- *:* with Student (via ParentStudentLink)

**JPA Entity**:
```java
@Entity
@Table(name = "parents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    @Column(name = "mobile_number", length = 15, nullable = false, unique = true)
    private String mobileNumber;
    
    @Column(length = 100)
    private String email;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(length = 100)
    private String occupation;
    
    @OneToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<ParentStudentLink> studentLinks;
}
```

---

### 4. ParentStudentLink

**Purpose**: Many-to-many relationship between parents and students

**Table**: `parent_student_links`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| parent_id | BIGINT | FK → parents(id), NOT NULL | Parent reference |
| student_id | BIGINT | FK → students(id), NOT NULL | Student reference |
| relationship_type | VARCHAR(50) | NOT NULL | Father/Mother/Guardian/Other |
| is_primary | BOOLEAN | NOT NULL, DEFAULT false | Primary guardian flag |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Linkage creation time |

**Indexes**:
- PRIMARY KEY (id)
- INDEX idx_parent_links (parent_id)
- INDEX idx_student_links (student_id)
- UNIQUE INDEX idx_parent_student_unique (parent_id, student_id)

**Relationships**:
- *:1 with Parent
- *:1 with Student

**Business Rules**:
- One parent can be linked to multiple students (e.g., siblings)
- One student can have multiple parents (e.g., both father and mother)
- At most one linkage per parent-student pair should be marked `is_primary=true`

**JPA Entity**:
```java
@Entity
@Table(name = "parent_student_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentStudentLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "parent_id", nullable = false)
    private Long parentId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", length = 50, nullable = false)
    private RelationshipType relationshipType;
    
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Parent parent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;
}

public enum RelationshipType {
    FATHER, MOTHER, GUARDIAN, OTHER
}
```

---

### 5. OtpVerification

**Purpose**: Temporary storage for OTP codes during authentication

**Table**: `otp_verifications`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| mobile_number | VARCHAR(15) | NOT NULL | Target mobile number |
| otp_code | VARCHAR(255) | NOT NULL | Encrypted OTP code |
| purpose | VARCHAR(50) | NOT NULL | login/password_reset |
| expires_at | TIMESTAMP | NOT NULL | Expiry time (5 minutes from creation) |
| is_used | BOOLEAN | NOT NULL, DEFAULT false | Usage flag |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |

**Indexes**:
- PRIMARY KEY (id)
- INDEX idx_otp_mobile_active (mobile_number, is_used, expires_at)

**TTL Strategy**: Cleanup job deletes records older than 24 hours

**JPA Entity**:
```java
@Entity
@Table(name = "otp_verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "mobile_number", length = 15, nullable = false)
    private String mobileNumber;
    
    @Column(name = "otp_code", nullable = false)
    private String otpCode; // Encrypted
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private OtpPurpose purpose;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

public enum OtpPurpose {
    LOGIN, PASSWORD_RESET
}
```

---

### 6. RefreshToken

**Purpose**: Track active refresh tokens for JWT rotation

**Table**: `refresh_tokens`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| user_id | BIGINT | FK → users(id), NOT NULL | Token owner |
| token_hash | VARCHAR(255) | NOT NULL, UNIQUE | BCrypt hashed token |
| device_id | VARCHAR(255) | NULL | Optional device identifier |
| expires_at | TIMESTAMP | NOT NULL | Token expiry (30 days) |
| is_revoked | BOOLEAN | NOT NULL, DEFAULT false | Revocation flag |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |

**Indexes**:
- PRIMARY KEY (id)
- UNIQUE INDEX idx_token_hash (token_hash)
- INDEX idx_user_active (user_id, is_revoked, expires_at)

**TTL Strategy**: Cleanup job deletes revoked tokens or expired tokens older than 90 days

**JPA Entity**:
```java
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
```

---

### 7. HomeworkSubmission

**Purpose**: Track student homework submissions with file attachments

**Table**: `homework_submissions`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| homework_id | BIGINT | FK → homework(id), NOT NULL | Assignment reference |
| student_id | BIGINT | FK → students(id), NOT NULL | Student who submitted |
| submitted_at | TIMESTAMP | NOT NULL | Submission timestamp |
| submission_file_urls | JSON | NULL | Array of S3 object keys/URLs |
| remarks | TEXT | NULL | Student's submission remarks |
| status | VARCHAR(50) | NOT NULL | submitted/late/reviewed |
| grade | VARCHAR(10) | NULL | Teacher assigned grade (A+, B, etc.) |
| teacher_remarks | TEXT | NULL | Teacher feedback |
| reviewed_at | TIMESTAMP | NULL | Teacher review timestamp |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Update time |

**Indexes**:
- PRIMARY KEY (id)
- UNIQUE INDEX idx_homework_student (homework_id, student_id) - one submission per student per homework
- INDEX idx_student_submissions (student_id, submitted_at DESC)

**JPA Entity**:
```java
@Entity
@Table(name = "homework_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeworkSubmission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "homework_id", nullable = false)
    private Long homeworkId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();
    
    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "submission_file_urls", columnDefinition = "JSON")
    private List<String> submissionFileUrls;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private SubmissionStatus status;
    
    @Column(length = 10)
    private String grade;
    
    @Column(name = "teacher_remarks", columnDefinition = "TEXT")
    private String teacherRemarks;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id", insertable = false, updatable = false)
    private Homework homework;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;
}

public enum SubmissionStatus {
    SUBMITTED, LATE, REVIEWED
}
```

---

## Transport/Bus Tracking Entities

### 8. Vehicle

**Purpose**: School transport vehicles (buses, vans)

**Table**: `vehicles`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| vehicle_number | VARCHAR(20) | NOT NULL, UNIQUE | Registration number |
| vehicle_type | VARCHAR(20) | NOT NULL | Bus/Van/Car |
| capacity | INTEGER | NOT NULL | Passenger capacity |
| driver_name | VARCHAR(100) | NOT NULL | Current driver name |
| driver_mobile | VARCHAR(15) | NOT NULL | Driver contact |
| driver_license | VARCHAR(50) | NULL | Driver's license number |
| gps_device_id | VARCHAR(100) | NULL | GPS tracker device ID |
| insurance_expiry | DATE | NULL | Insurance validity |
| last_maintenance_date | DATE | NULL | Last service date |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | Active status |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Update time |

**Indexes**:
- PRIMARY KEY (id)
- UNIQUE INDEX idx_vehicle_number (vehicle_number)
- INDEX idx_gps_device (gps_device_id)

---

### 9. Route

**Purpose**: Bus routes with start/end times

**Table**: `routes`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| route_name | VARCHAR(100) | NOT NULL | Route name (e.g., "East Zone Morning") |
| route_number | VARCHAR(20) | NOT NULL, UNIQUE | Route identifier |
| vehicle_id | BIGINT | FK → vehicles(id), NULL | Assigned vehicle |
| start_time | TIME | NOT NULL | Route start time |
| end_time | TIME | NOT NULL | Route end time |
| total_distance_km | DECIMAL(6,2) | NULL | Total route distance |
| estimated_duration_minutes | INTEGER | NULL | Expected duration |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | Active status |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Update time |

**Indexes**:
- PRIMARY KEY (id)
- UNIQUE INDEX idx_route_number (route_number)
- INDEX idx_route_vehicle (vehicle_id)

---

### 10. Stop

**Purpose**: Bus stops on a route

**Table**: `stops`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| route_id | BIGINT | FK → routes(id), NOT NULL | Parent route |
| stop_name | VARCHAR(200) | NOT NULL | Stop name |
| stop_order | INTEGER | NOT NULL | Sequence in route (1, 2, 3...) |
| latitude | DECIMAL(10,8) | NOT NULL | GPS latitude |
| longitude | DECIMAL(11,8) | NOT NULL | GPS longitude |
| scheduled_arrival_time | TIME | NOT NULL | Expected arrival time |
| landmark_description | VARCHAR(255) | NULL | Nearby landmark |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |

**Indexes**:
- PRIMARY KEY (id)
- UNIQUE INDEX idx_route_stop_order (route_id, stop_order)
- INDEX idx_route_stops (route_id, stop_order ASC)

---

### 11. Trip

**Purpose**: Individual bus journey instance

**Table**: `trips`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| route_id | BIGINT | FK → routes(id), NOT NULL | Route reference |
| vehicle_id | BIGINT | FK → vehicles(id), NOT NULL | Vehicle used |
| trip_date | DATE | NOT NULL | Journey date |
| trip_type | VARCHAR(20) | NOT NULL | morning_pickup/afternoon_drop |
| status | VARCHAR(20) | NOT NULL | scheduled/in_progress/completed/cancelled |
| start_time | TIMESTAMP | NULL | Actual start time |
| end_time | TIMESTAMP | NULL | Actual end time |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |

**Indexes**:
- PRIMARY KEY (id)
- UNIQUE INDEX idx_route_date_type (route_id, trip_date, trip_type)
- INDEX idx_trip_status_date (status, trip_date DESC)

---

### 12. StudentTransportAssignment

**Purpose**: Assign students to routes and stops

**Table**: `student_transport_assignments`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| student_id | BIGINT | FK → students(id), NOT NULL | Assigned student |
| route_id | BIGINT | FK → routes(id), NOT NULL | Assigned route |
| pickup_stop_id | BIGINT | FK → stops(id), NOT NULL | Morning pickup stop |
| drop_stop_id | BIGINT | FK → stops(id), NOT NULL | Afternoon drop stop |
| effective_from | DATE | NOT NULL | Assignment start date |
| effective_to | DATE | NULL | Assignment end date (null = ongoing) |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | Active status |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |

**Indexes**:
- PRIMARY KEY (id)
- INDEX idx_student_active_assignment (student_id, is_active, effective_to)
- INDEX idx_route_students (route_id, is_active)

---

### 13. LocationPing

**Purpose**: GPS tracking data (time-series, high volume)

**Table**: `location_pings` (partitioned by `captured_at`)

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | Primary key |
| vehicle_id | BIGINT | FK → vehicles(id), NOT NULL | Vehicle reporting location |
| trip_id | BIGINT | FK → trips(id), NULL | Associated trip (if known) |
| latitude | DECIMAL(10,8) | NOT NULL | GPS latitude |
| longitude | DECIMAL(11,8) | NOT NULL | GPS longitude |
| speed | DECIMAL(5,2) | NULL | Speed in km/h |
| heading | SMALLINT | NULL | Direction in degrees (0-359) |
| accuracy | DECIMAL(6,2) | NULL | GPS accuracy in meters |
| captured_at | TIMESTAMP | NOT NULL | Device timestamp |
| received_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Server receipt timestamp |

**Partitioning**: Daily or weekly partitions on `captured_at`

**Indexes**:
- PRIMARY KEY (id, captured_at) -- Include partition key
- INDEX idx_location_vehicle_time (vehicle_id, captured_at DESC)

**Retention**: 30 days, automated partition dropping

---

### 14. TransportEvent

**Purpose**: Significant transport events (pickup, drop, alerts)

**Table**: `transport_events`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| trip_id | BIGINT | FK → trips(id), NOT NULL | Associated trip |
| student_id | BIGINT | FK → students(id), NULL | Related student (if applicable) |
| event_type | VARCHAR(50) | NOT NULL | bus_start/stop_arrival/student_pickup/student_drop/trip_complete |
| event_time | TIMESTAMP | NOT NULL | Event occurrence time |
| latitude | DECIMAL(10,8) | NULL | Event location latitude |
| longitude | DECIMAL(11,8) | NULL | Event location longitude |
| remarks | TEXT | NULL | Additional notes |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |

**Indexes**:
- PRIMARY KEY (id)
- INDEX idx_trip_events (trip_id, event_time DESC)
- INDEX idx_student_events (student_id, event_time DESC)

---

## Notification Entities

### 15. NotificationToken

**Purpose**: Store FCM/APNs device tokens

**Table**: `notification_tokens`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| user_id | BIGINT | FK → users(id), NOT NULL | Token owner |
| token | VARCHAR(500) | NOT NULL, UNIQUE | FCM/APNs token string |
| platform | VARCHAR(20) | NOT NULL | android/ios |
| device_id | VARCHAR(255) | NULL | Device identifier |
| registered_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Registration time |
| last_used_at | TIMESTAMP | NULL | Last successful send time |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | Active status |

**Indexes**:
- PRIMARY KEY (id)
- UNIQUE INDEX idx_token (token)
- INDEX idx_user_active_tokens (user_id, is_active)

---

### 16. NotificationLog

**Purpose**: Audit trail for sent notifications

**Table**: `notification_logs`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| user_id | BIGINT | FK → users(id), NOT NULL | Notification recipient |
| notification_type | VARCHAR(50) | NOT NULL | absence/fee_due/notice/exam/bus_alert |
| title | VARCHAR(255) | NOT NULL | Notification title |
| body | TEXT | NOT NULL | Notification body |
| sent_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Send time |
| delivery_status | VARCHAR(20) | NOT NULL | pending/sent/delivered/failed |
| read_at | TIMESTAMP | NULL | Read timestamp (if tracked) |

**Indexes**:
- PRIMARY KEY (id)
- INDEX idx_user_notifications (user_id, sent_at DESC)
- INDEX idx_notification_type_date (notification_type, sent_at DESC)

---

## Notice Entities (If Not Exists)

### 17. Notice

**Purpose**: School-wide or class-specific notices

**Table**: `notices`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| title | VARCHAR(255) | NOT NULL | Notice title |
| description | TEXT | NOT NULL | Notice content |
| publish_date | DATE | NOT NULL | Publication date |
| priority | VARCHAR(20) | NOT NULL | low/medium/high |
| target_role | VARCHAR(50) | NULL | Targeted role (null = all) |
| target_class_ids | JSON | NULL | Array of class IDs (null = all) |
| attachment_urls | JSON | NULL | Array of file URLs |
| created_by | BIGINT | FK → users(id), NOT NULL | Notice creator |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation time |

**Indexes**:
- PRIMARY KEY (id)
- INDEX idx_publish_priority (publish_date DESC, priority)

---

### 18. NoticeReadStatus

**Purpose**: Track notice read status per user

**Table**: `notice_read_statuses`

**Fields**:
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Primary key |
| user_id | BIGINT | FK → users(id), NOT NULL | User who read |
| notice_id | BIGINT | FK → notices(id), NOT NULL | Notice read |
| read_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Read time |

**Indexes**:
- PRIMARY KEY (id)
- UNIQUE INDEX idx_user_notice (user_id, notice_id)

---

## Database Migration Order

Flyway migrations must be created in dependency order:

1. **V8__add_role_parent.sql** - ALTER RoleName enum
2. **V9__add_student_user_id.sql** - ALTER students table
3. **V10__create_parent_tables.sql** - Parent, ParentStudentLink
4. **V11__create_otp_refresh_tables.sql** - OtpVerification, RefreshToken
5. **V12__create_homework_submission.sql** - HomeworkSubmission
6. **V13__create_transport_tables.sql** - Vehicle, Route, Stop, Trip, StudentTransportAssignment, LocationPing (partitioned), TransportEvent
7. **V14__create_notification_tables.sql** - NotificationToken, NotificationLog
8. **V15__create_notice_tables.sql** - Notice, NoticeReadStatus (if not exists)

---

**Phase 1: Data Model Complete** - All entities defined with relationships, constraints, and indexes. Ready for contract generation.
