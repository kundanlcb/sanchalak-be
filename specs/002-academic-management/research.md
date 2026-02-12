# Research: Academic Management

## Unknowns & Decisions

### 1. Data Model for Exam Configuration
- **Question**: How to define "Max Marks" which varies per exam (Unit Test=20, Final=100) for the same Subject?
- **Decision**: Introduce `ExamSchedule` (or `ExamSubjectConfig`) entity linking `ExamTerm`, `Class`, and `Subject`.
- **Rationale**:
    - `Subject` (e.g., Math) is global.
    - `ClassSubject` (e.g., Math for 10A) links Teacher.
    - `ExamSchedule` (e.g., Math for 10A in Q1) defines `max_marks`, `exam_date`, `start_time`, `end_time`.
    - `StudentMarks` links to `ExamSchedule`.

### 2. Homework Attachments
- **Question**: How to handle PDF uploads for Homework?
- **Decision**: Store metadata (filename, URL) in `Homework` table. File storage likely S3/MinIO (out of scope for this spec, assume URL provided by frontend or separate implementation).
- **Rationale**: Keeps database light. For now, just string `attachmentUrl`.

### 3. Marks Validation Strategy
- **Question**: Where to enforce Max Marks validation?
- **Decision**: Service Layer + Database Constraint (if possible, but Service is primary).
- **Rationale**: `ExamSchedule.maxMarks` is dynamic row data, hard to enforce via SQL CHECK constraint easily without triggers. Service logic is sufficient.

## Proposed Data Model

```mermaid
erDiagram
    ExamTerm ||--o{ ExamSchedule : defines
    Class ||--o{ ClassSubject : has
    Subject ||--o{ ClassSubject : is_taught_in
    ClassSubject ||--o{ ExamSchedule : is_assessed_in
    ExamSchedule ||--o{ StudentMarks : has_results
    Student ||--o{ StudentMarks : obtains

    ExamTerm {
        string name "Mid-Term"
        date start_date
        date end_date
    }
    ExamSchedule {
        int max_marks
        date exam_date
    }
    StudentMarks {
        float marks_obtained
        string remarks
    }
```
