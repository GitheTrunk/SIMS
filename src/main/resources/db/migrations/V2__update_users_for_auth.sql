-- V2: Add username column and update roles (STUDENT -> USER, add COMPANY)

-- Add username column
ALTER TABLE users ADD COLUMN username VARCHAR(100);

-- Update existing records to have a username (from email prefix)
UPDATE users SET username = SUBSTRING_INDEX(email, '@', 1) WHERE username IS NULL;

-- Make username unique and not null
ALTER TABLE users MODIFY COLUMN username VARCHAR(100) NOT NULL UNIQUE;

-- Rename enabled to active for consistency
ALTER TABLE users CHANGE COLUMN enabled active BOOLEAN DEFAULT TRUE;

-- Update existing STUDENT role to USER
UPDATE users SET role = 'USER' WHERE role = 'STUDENT';

-- Change role column from ENUM to VARCHAR to match Hibernate expectations
ALTER TABLE users MODIFY COLUMN role VARCHAR(255) DEFAULT 'USER' NOT NULL;
