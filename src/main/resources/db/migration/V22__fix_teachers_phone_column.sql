-- V22: Fix teachers table column mismatch
-- Remove duplicate mobile_number column that was manually added
-- The Teacher entity correctly uses 'phone' column (mapped from mobileNumber field)

-- Drop mobile_number column if it exists (this was manually added and conflicts with the entity mapping)
ALTER TABLE teachers 
DROP COLUMN IF EXISTS mobile_number;

-- Ensure phone column is properly set up (should already exist from V21)
-- Make it NOT NULL after existing data is migrated
DO $$
BEGIN
    -- Check if phone column exists and has NULL constraint
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'teachers' 
        AND column_name = 'phone'
        AND is_nullable = 'YES'
    ) THEN
        -- First, update any NULL values with a default
        UPDATE teachers SET phone = '' WHERE phone IS NULL;
        
        -- Then make it NOT NULL
        ALTER TABLE teachers ALTER COLUMN phone SET NOT NULL;
    END IF;
END $$;

-- Add comment for documentation
COMMENT ON COLUMN teachers.phone IS 'Mobile/phone number - mapped from mobileNumber field in Java entity. Do not add mobile_number column.';
