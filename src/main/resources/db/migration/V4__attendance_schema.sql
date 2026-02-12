CREATE TABLE attendance_records (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks VARCHAR(255),
    marked_by VARCHAR(100),
    modified_by VARCHAR(100),
    is_modified BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_attendance_student_id FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_attendance_class_id FOREIGN KEY (class_id) REFERENCES classes (id),
    CONSTRAINT uk_attendance_student_date UNIQUE (student_id, date)
);

CREATE INDEX idx_attendance_class_date ON attendance_records(class_id, date);
CREATE INDEX idx_attendance_student ON attendance_records(student_id);
