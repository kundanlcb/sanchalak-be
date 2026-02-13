-- V18: Update schema for Phase 8 and 9 requirements

-- 1. Update notices table (from V15)
-- Add expiry_date
ALTER TABLE notices ADD COLUMN expiry_date DATE NULL AFTER publish_date;

-- Add attachment_url (singular) if not exists (V15 has attachment_urls JSON)
-- We'll add singular column for simplicity of Phase 9 implementation
ALTER TABLE notices ADD COLUMN attachment_url VARCHAR(500) NULL AFTER is_active;

-- Rename description to content (if you prefer 'content' in code) OR just map it in Entity
-- Let's rename column to match code 'content'
ALTER TABLE notices CHANGE COLUMN description content TEXT NOT NULL;


-- 2. Update notification_tokens table (from V14)
-- Increase token length to 500 (V14 was 255)
ALTER TABLE notification_tokens MODIFY COLUMN token VARCHAR(500) NOT NULL;

-- 3. Update notification_logs table (from V14)
-- Rename body to message to match Entity
ALTER TABLE notification_logs CHANGE COLUMN body message TEXT NOT NULL;

-- Add missing columns from my Entity design
ALTER TABLE notification_logs ADD COLUMN target_token VARCHAR(500) NULL AFTER data_payload;
ALTER TABLE notification_logs ADD COLUMN platform VARCHAR(20) NULL AFTER target_token;
ALTER TABLE notification_logs ADD COLUMN reference_id VARCHAR(100) NULL AFTER error_message;
ALTER TABLE notification_logs ADD COLUMN reference_type VARCHAR(50) NULL AFTER reference_id;

-- Ensure data_payload is TEXT (V14 was JSON, which is fine, but Entity expects String/Text)
-- If MySQL 5.7+, JSON is good. JPA can map it. 
-- But my code does: log.setDataPayload(objectMapper.writeValueAsString(data));
-- If it's JSON type in DB, writing string might fail unless it's valid JSON.
-- Let's keep it JSON if it is, but if we want simple TEXT:
ALTER TABLE notification_logs MODIFY COLUMN data_payload TEXT NULL;
