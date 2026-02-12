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
