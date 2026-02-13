-- V11__create_otp_refresh_tables.sql
-- Create OTP verification and refresh token tables for mobile authentication

-- Table 1: otp_verifications (temporary OTP storage)
CREATE TABLE otp_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mobile_number VARCHAR(15) NOT NULL,
    otp_code VARCHAR(255) NOT NULL COMMENT 'AES-256 encrypted',
    purpose VARCHAR(20) NOT NULL DEFAULT 'LOGIN',
    attempt_count INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_otp_mobile_number (mobile_number),
    INDEX idx_otp_expiry (expires_at),
    INDEX idx_otp_lookup (mobile_number, is_used, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 2: refresh_tokens (token rotation tracking)
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE COMMENT 'BCrypt hashed',
    device_id VARCHAR(100) NULL,
    device_type VARCHAR(20) NULL COMMENT 'android, ios, web',
    token_family_id VARCHAR(100) NULL COMMENT 'Track token families for theft detection',
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,
    last_used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_hash (token_hash),
    INDEX idx_refresh_token_expiry (expires_at),
    INDEX idx_refresh_token_family (token_family_id),
    INDEX idx_refresh_token_active (user_id, is_revoked, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comments
ALTER TABLE otp_verifications COMMENT = 'Temporary OTP storage with encryption and expiry';
ALTER TABLE refresh_tokens COMMENT = 'JWT refresh tokens with rotation and revocation support';

-- Create cleanup job hint (actual job implemented in application code)
-- OTP records expire after 5 minutes and should be cleaned up daily
-- Refresh tokens expire after 30 days and should be cleaned up weekly
