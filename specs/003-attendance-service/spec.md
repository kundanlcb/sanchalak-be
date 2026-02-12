# Feature Specification: Attendance Service

**Feature Branch**: `003-attendance-service`
**Status**: Draft

## User Scenarios & Testing

### User Story 1 - Mark Daily Attendance (Priority: P1)

As a **Teacher**, I want to **mark attendance for a class on a specific date**, so that **school records track student presence**.

**Why this priority**: Core function of the attendance module. Without inputting data, the system has no value.

**Independent Test**:
- Create a class with students.
- Log in as Teacher.
- Submit attendance for the class for "Today".
- Verify records are saved in the database.

**Acceptance Scenarios**:

1. **Given** a class valid students, **When** Teacher submits "Present" for all students for today, **Then** the system saves `PRESENT` status for each student for that date.
2. **Given** a class, **When** Teacher marks specific students as "Absent", **Then** the system saves `ABSENT` status for those students.
3. **Given** attendance already exists for a date, **When** Teacher submits update, **Then** the records are updated.

---

### User Story 2 - View Student Attendance History (Priority: P2)

As a **Student or Parent**, I want to **view my attendance history**, so that **I can track my attendance percentage**.

**Why this priority**: Provides visibility to end-users (students/parents) which is a key Sanchalak value proposition.

**Independent Test**:
- Log in as Student.
- Request attendance history.
- Verify the list matches the data entered by the teacher.

**Acceptance Scenarios**:

1. **Given** a student with 20 days of attendance, **When** they view their profile/dashboard, **Then** they see a summary (e.g., "90% Attendance") and a list of absent days.

---

### User Story 3 - Admin Attendance Reports (Priority: P3)

As an **Admin**, I want to **view attendance stats for classes**, so that **I can identify low-attendance trends**.

**Why this priority**: Management needs oversight, but individual data entry (US1) and student view (US2) come first.

**Independent Test**:
- Log in as Admin.
- Request report for Class X.
- Verify aggregation is correct.

**Acceptance Scenarios**:

1. **Given** multiple days of data, **When** Admin requests monthly report for Class 5A, **Then** system returns the aggregated percentage for the class.

### Edge Cases

- **Future Dates**: Users cannot mark attendance for future dates. System should block/error.
- **Holidays/Weekends**: Marking attendance on non-school days should be allowed (extra class) but perhaps warned? (Assume allowed for simplicity).
- **Duplicate Marking**: If a teacher marks attendance twice, the second entry updates the first.
- **Student not in Class**: If a student is transferred, their old attendance records remain linked to the old class snapshot (referenced by class_id).

## Requirements

### Functional Requirements

- **FR-001**: System MUST allow Teachers and Admins to mark attendance for a group of students (Class) for a specific date (Bulk Operation).
- **FR-002**: System MUST allow marking attendance for an individual student (Single Operation).
- **FR-003**: System MUST defaults attendance status to `Present` if not specified during bulk creation.
- **FR-004**: System MUST support statuses: `Present`, `Absent`, `Late`, `Excused`, `Holiday` (Case-insensitive mapping preferred, standardized to Backend Enum).
- **FR-005**: System MUST prevent duplicate attendance records for the same student on the same date (updates should overwrite or be handled via PUT).
- **FR-006**: Students MUST only have read access to their own attendance records.
- **FR-007**: System MUST validate that the date is not in the future.
- **FR-008**: System MUST track who marked the attendance and when (Audit Trail: `markedBy`, `markedDate`, `modifiedBy`, `modifiedDate`).
- **FR-009**: System MUST provide summary statistics (percentage, counts) compatible with the Frontend Dashboard.
- **FR-010**: System MUST support retrieving a "Class Attendance Sheet" for a specific date, including aggregated counts (Present/Absent/etc.).

### Key Entities

- **AttendanceRecord**:
  - `id`: Unique identifier (UUID or Long).
  - `student_id`: Reference to Student.
  - `class_id`: Reference to Class.
  - `date`: Date of attendance (YYYY-MM-DD).
  - `status`: Enum (PRESENT, ABSENT, LATE, EXCUSED, HOLIDAY).
  - `remarks`: Optional text.
  - `is_modified`: Boolean flag.
  - `marked_by`: User ID of the marker.
  - `marked_date`: Timestamp of creation.
  - `modified_by`: User ID of the modifier.
  - `modified_date`: Timestamp of last modification.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Teachers can mark attendance for a class of 40 students in under 1 minute.
- **SC-002**: System prevents any attempt to mark attendance for future dates.
- **SC-003**: Attendance reports for a class (e.g., 1 month data) load in under 2 seconds.
- **SC-004**: 100% of attendance records link to a valid Student and Class.
