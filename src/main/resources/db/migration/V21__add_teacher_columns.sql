-- V21: Add missing columns to teachers table
-- This migration adds all required columns that the Teacher entity expects

-- Add email column (required, unique)
ALTER TABLE teachers 
ADD COLUMN email VARCHAR(50) NULL UNIQUE;

-- Add phone column (required) - mapped from mobileNumber field in Java entity
ALTER TABLE teachers 
ADD COLUMN phone VARCHAR(15) NULL;

-- Add optional columns
ALTER TABLE teachers 
ADD COLUMN qualification VARCHAR(100) NULL;

ALTER TABLE teachers 
ADD COLUMN profile_image VARCHAR(500) NULL;

ALTER TABLE teachers 
ADD COLUMN teacher_id VARCHAR(50) NULL UNIQUE;

ALTER TABLE teachers 
ADD COLUMN joining_date VARCHAR(50) NULL;

-- Add soft delete flag with default value
ALTER TABLE teachers 
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Create indexes for performance
CREATE INDEX idx_teachers_deleted ON teachers(deleted);
CREATE INDEX idx_teachers_email ON teachers(email);

-- Create teacher_specializations junction table for many-to-many relationship with subjects
CREATE TABLE IF NOT EXISTS teacher_specializations (
    teacher_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    PRIMARY KEY (teacher_id, subject_id),
    CONSTRAINT fk_teacher_spec_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
    CONSTRAINT fk_teacher_spec_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

-- Add comment for documentation
COMMENT ON TABLE teachers IS 'Teachers/faculty members with their qualifications and contact information';
COMMENT ON COLUMN teachers.phone IS 'Mobile number - mapped from mobileNumber field in Java entity';
COMMENT ON COLUMN teachers.deleted IS 'Soft delete flag - true means teacher is inactive/deleted';
