-- V10__create_parent_tables.sql
-- Create Parent entity and ParentStudentLink for many-to-many relationship

-- Table 1: parents
CREATE TABLE parents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BINARY(16) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NULL,
    mobile_number VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100) NULL,
    address TEXT NULL,
    occupation VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_parent_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for parents
CREATE INDEX idx_parent_user_id ON parents(user_id);
CREATE INDEX idx_parent_mobile ON parents(mobile_number);

-- Table 2: parent_student_links (many-to-many relationship)
CREATE TABLE parent_student_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    relationship_type VARCHAR(20) NOT NULL DEFAULT 'GUARDIAN',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_date DATE NULL,
    end_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_parent_link_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    CONSTRAINT fk_parent_link_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    
    -- Prevent duplicate linkages
    CONSTRAINT uk_parent_student UNIQUE (parent_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for parent_student_links (optimized for parent-child lookups)
CREATE INDEX idx_parent_student_link_parent ON parent_student_links(parent_id);
CREATE INDEX idx_parent_student_link_student ON parent_student_links(student_id);
CREATE INDEX idx_parent_student_link_active ON parent_student_links(parent_id, student_id, is_active);
CREATE INDEX idx_parent_student_link_composite ON parent_student_links(parent_id, is_active, effective_date);

-- Add comments for documentation
ALTER TABLE parents COMMENT = 'Parent/guardian accounts for mobile app access';
ALTER TABLE parent_student_links COMMENT = 'Many-to-many relationship between parents and students with authorization context';
