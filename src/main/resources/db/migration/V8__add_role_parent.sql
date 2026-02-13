-- V8__add_role_parent.sql
-- Add ROLE_PARENT to RoleName enum support

-- Step 1: Add ROLE_PARENT entry to roles table if it doesn't exist
INSERT INTO roles (name) 
SELECT 'ROLE_PARENT' 
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_PARENT');
