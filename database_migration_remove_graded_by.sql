-- SQL Migration: Remove graded_by column from submissions table
-- This migration removes the graded_by foreign key constraint and column
-- since we can derive the grader information from assignment.course.teacher

-- Step 1: Drop foreign key constraint first
-- The constraint name from your error is: FKmjfva3nkq8vlatkp2mk3678hh
ALTER TABLE submissions DROP FOREIGN KEY FKmjfva3nkq8vlatkp2mk3678hh;

-- Step 2: Drop the graded_by column
ALTER TABLE submissions DROP COLUMN graded_by;

-- Step 3: Verification - Check table structure
DESCRIBE submissions;

-- Expected result: graded_by column should no longer exist
-- The remaining columns should be:
-- - id
-- - file_path  
-- - submitted_at
-- - graded_at
-- - score
-- - feedback
-- - status
-- - is_late
-- - assignment_id
-- - student_id

-- Optional: Check foreign key constraints to confirm removal
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_NAME = 'submissions' 
    AND TABLE_SCHEMA = DATABASE()
    AND REFERENCED_TABLE_NAME IS NOT NULL;
