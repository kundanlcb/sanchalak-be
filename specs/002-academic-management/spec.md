# Feature Specification: Academic Management

**Feature Branch**: `002-academic-management`
**Created**: 2026-02-12
**Status**: Draft
**Input**: Academic Core: Exams, Marks, Homework, Report Cards.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Exam & Subject Management (Priority: P1)

Admins need to define structure: Exam Terms (Quarterly, Final), Subjects (Math, Science), and map simple subjects to Classes.

**Why this priority**: Prerequisite for marks entry.

**Independent Test**: Create Exam "Mid-Term", Create Subject "Math", Link Math to Class 10A.

**Acceptance Scenarios**:
1. **Given** Admin token, **When** `POST /api/academic/terms`, **Then** Exam Term created.
2. **Given** Admin token, **When** `POST /api/academic/subjects`, **Then** Subject created.
3. **Given** Class 10A, **When** `POST /api/academic/classes/10A/subjects`, **Then** Subject is linked to Class.

### User Story 2 - Marks Entry (Priority: P1)

Teachers need to save marks for students in their subjects.

**Why this priority**: Core academic data.

**Independent Test**: Post marks for a student in a specific exam/subject.

**Acceptance Scenarios**:
1. **Given** Teacher token, **When** `POST /api/academic/marks` with valid score (e.g., 85/100), **Then** saved successfully.
2. **Given** marks > max marks, **When** POST, **Then** 400 Bad Request.
3. **Given** non-teacher/wrong teacher, **When** POST, **Then** 403 Forbidden.

### User Story 3 - Report Card Data (Priority: P2)

Admins need aggregated data to generate PDF reports.

**Why this priority**: Required for output generation.

**Independent Test**: Call `GET /api/academic/reports/card/{studentId}` and receive JSON with all term marks.

**Acceptance Scenarios**:
1. **Given** student with marks in 3 subjects, **When** GET report, **Then** returns subject names, marks obtained, max marks, and calculated grade.

### User Story 4 - Homework Management (Priority: P3)

Teachers post homework assignments.

**Why this priority**: Day-to-day operations.

**Independent Test**: Create homework, Fetch homework for class.

**Acceptance Scenarios**:
1. **Given** Teacher, **When** `POST /api/homework` for Class 10A, **Then** created.
2. **Given** Student in 10A, **When** `GET /api/homework`, **Then** sees assignments.

## Functional Requirements

### Functional Requirements

- **FR-001**: System MUST allow creating Exam Terms (name, start_date, end_date).
- **FR-002**: System MUST allow defining Subjects and mapping 'Subject' to 'Class' (ClassSubject).
- **FR-003**: System MUST allow recording marks for (Student, Exam, Subject).
- **FR-004**: System MUST validate marks do not exceed defined maximum for the subject/exam.
- **FR-005**: System MUST allow CRUD for Homework (title, description, due_date) linked to a Class and Subject.

### Key Entities

- **ExamTerm**: definition of exam period.
- **Subject**: Global subject list (Math, Science).
- **ClassSubject**: Mapping of Subject -> Class (e.g., "Math for Class 10A").
- **StudentMarks**: Score record.
- **Homework**: Assignment details.
