# Data Model: Academic Management

## Entities

### 1. ExamTerm
Represents a major assessment period (e.g., "Annual Exam 2026").
- `id`: Long (PK)
- `name`: String (Unique)
- `startDate`: LocalDate
- `endDate`: LocalDate
- `isActive`: Boolean

### 2. Subject
Global list of subjects taught.
- `id`: Long (PK)
- `name`: String (Unique, e.g., "Mathematics")
- `code`: String (Unique, e.g., "MATH")

### 3. ClassSubject
Mapping of subjects to classes, optionally assigning a specific teacher.
- `id`: Long (PK)
- `class`: Class (FK)
- `subject`: Subject (FK)
- `teacher`: Teacher (FK, Nullable)

### 4. ExamSchedule
Defines a specific paper/test within an Exam Term for a Class.
- `id`: Long (PK)
- `examTerm`: ExamTerm (FK)
- `class`: Class (FK)
- `subject`: Subject (FK)
- `maxMarks`: Integer (Not Null, > 0)
- `examDate`: LocalDate

### 5. StudentMarks
The score obtained by a student.
- `id`: Long (PK)
- `student`: Student (FK)
- `examSchedule`: ExamSchedule (FK)
- `marksObtained`: Double (Not Null, separate from maxMarks)
- `remarks`: String

### 6. Homework
Assignments posted by teachers.
- `id`: Long (PK)
- `class`: Class (FK)
- `subject`: Subject (FK)
- `teacher`: Teacher (FK)
- `title`: String
- `description`: Text
- `dueDate`: LocalDate
- `attachmentUrl`: String (Nullable)

## SQL Schema (Flyway V3)

```sql
CREATE TABLE exam_terms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE class_subjects (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    teacher_id BIGINT REFERENCES teachers(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(class_id, subject_id)
);

CREATE TABLE exam_schedules (
    id BIGSERIAL PRIMARY KEY,
    exam_term_id BIGINT NOT NULL REFERENCES exam_terms(id),
    class_id BIGINT NOT NULL REFERENCES classes(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    max_marks INTEGER NOT NULL,
    exam_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(exam_term_id, class_id, subject_id)
);

CREATE TABLE student_marks (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id),
    exam_schedule_id BIGINT NOT NULL REFERENCES exam_schedules(id),
    marks_obtained DOUBLE PRECISION NOT NULL,
    remarks VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(student_id, exam_schedule_id)
);

CREATE TABLE homework (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    teacher_id BIGINT NOT NULL REFERENCES teachers(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_date DATE NOT NULL,
    attachment_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```
