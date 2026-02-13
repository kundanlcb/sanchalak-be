-- V19: Create Audit Log table for Phase 10

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NULL,
    resource_id VARCHAR(100) NULL,
    details TEXT NULL,
    ip_address VARCHAR(50) NULL,
    user_agent VARCHAR(200) NULL,
    status VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action_type),
    INDEX idx_audit_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE audit_logs COMMENT = 'Security and access audit logs';
