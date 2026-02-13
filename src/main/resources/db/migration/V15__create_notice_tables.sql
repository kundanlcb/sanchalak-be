-- V15__create_notice_tables.sql
-- Create notices and notice read status tables (if not already exists)

-- Table 1: notices
CREATE TABLE IF NOT EXISTS notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    publish_date DATE NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    target_role VARCHAR(30) NULL COMMENT 'NULL = all roles, or specific: STUDENT, PARENT, TEACHER',
    target_class_ids JSON NULL COMMENT 'Array of class IDs, NULL = all classes',
    attachment_urls JSON NULL COMMENT 'Array of file URLs',
    created_by_user_id BINARY(16) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notice_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_notice_publish_date (publish_date),
    INDEX idx_notice_priority (priority),
    INDEX idx_notice_target_role (target_role),
    INDEX idx_notice_active (is_active, publish_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 2: notice_read_status
CREATE TABLE IF NOT EXISTS notice_read_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    notice_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notice_read_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notice_read_notice FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE,
    
    -- Prevent duplicate read records
    CONSTRAINT uk_user_notice UNIQUE (user_id, notice_id),
    
    INDEX idx_notice_read_user (user_id),
    INDEX idx_notice_read_notice (notice_id),
    INDEX idx_notice_read_lookup (user_id, notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comments
ALTER TABLE notices COMMENT = 'School-wide or targeted notices/announcements';
ALTER TABLE notice_read_status COMMENT = 'Track which users have read which notices';
