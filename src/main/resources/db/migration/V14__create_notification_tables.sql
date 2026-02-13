-- V14__create_notification_tables.sql
-- Create notification token and log tables for FCM/APNs push notifications

-- Table 1: notification_tokens (device registration)
CREATE TABLE notification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE COMMENT 'FCM or APNs device token',
    platform VARCHAR(20) NOT NULL,
    device_id VARCHAR(100) NULL,
    device_model VARCHAR(100) NULL,
    app_version VARCHAR(20) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notification_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_notification_token_user (user_id),
    INDEX idx_notification_token_token (token),
    INDEX idx_notification_token_platform (platform),
    INDEX idx_notification_token_active (user_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 2: notification_logs (audit trail)
CREATE TABLE notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    data_payload JSON NULL COMMENT 'Additional structured data',
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    delivered_at TIMESTAMP NULL,
    read_at TIMESTAMP NULL,
    fcm_message_id VARCHAR(255) NULL,
    error_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notification_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_notification_log_user (user_id),
    INDEX idx_notification_log_type (notification_type),
    INDEX idx_notification_log_status (delivery_status),
    INDEX idx_notification_log_sent_at (sent_at),
    INDEX idx_notification_log_user_type (user_id, notification_type, sent_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comments
ALTER TABLE notification_tokens COMMENT = 'FCM/APNs device token registration for push notifications';
ALTER TABLE notification_logs COMMENT = 'Audit trail for sent push notifications with delivery status';
