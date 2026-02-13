-- V12__create_homework_submission.sql
-- Create homework_submissions table for student homework file uploads

CREATE TABLE homework_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    homework_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submission_file_urls JSON NULL COMMENT 'Array of file URLs from S3/Azure',
    remarks TEXT NULL COMMENT 'Student remarks/notes',
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    grade VARCHAR(10) NULL,
    teacher_remarks TEXT NULL,
    graded_at TIMESTAMP NULL,
    graded_by_user_id BINARY(16) NULL,
    is_late BOOLEAN NOT NULL DEFAULT FALSE,
    resubmission_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_homework_submission_homework FOREIGN KEY (homework_id) REFERENCES homework(id) ON DELETE CASCADE,
    CONSTRAINT fk_homework_submission_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_homework_submission_graded_by FOREIGN KEY (graded_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Ensure one submission per student per homework (resubmission updates same record)
    CONSTRAINT uk_homework_student UNIQUE (homework_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for homework_submissions
CREATE INDEX idx_homework_submission_homework ON homework_submissions(homework_id);
CREATE INDEX idx_homework_submission_student ON homework_submissions(student_id);
CREATE INDEX idx_homework_submission_status ON homework_submissions(status);
CREATE INDEX idx_homework_submission_submitted_at ON homework_submissions(submitted_at);
CREATE INDEX idx_homework_submission_lookup ON homework_submissions(student_id, status, submitted_at);

-- Add comment
ALTER TABLE homework_submissions COMMENT = 'Student homework submissions with file attachments and grading';
