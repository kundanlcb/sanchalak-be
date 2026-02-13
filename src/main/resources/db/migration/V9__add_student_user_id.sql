-- V9__add_student_user_id.sql
-- Add user_id column to students table for direct student login
-- Add mobile_number to users table for OTP authentication

-- Step 1: Add mobile_number to users table if it doesn't exist
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS mobile_number VARCHAR(15) NULL UNIQUE;

CREATE INDEX IF NOT EXISTS idx_users_mobile_number ON users(mobile_number);

-- Step 2: Add user_id to students table (nullable for legacy data)
ALTER TABLE students 
ADD COLUMN IF NOT EXISTS user_id BINARY(16) NULL;

-- Step 3: Add foreign key constraint
ALTER TABLE students 
ADD CONSTRAINT IF NOT EXISTS fk_student_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Step 4: Create unique index (only for non-null values)
CREATE UNIQUE INDEX IF NOT EXISTS idx_student_user_id ON students(user_id);

-- Step 5: Add comment for documentation
ALTER TABLE students MODIFY COLUMN user_id BINARY(16) NULL 
    COMMENT 'Links student to user account for authentication. Nullable for legacy data.';
