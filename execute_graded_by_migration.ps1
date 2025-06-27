# PowerShell script to safely remove graded_by column
# Run this script to execute the database migration

# Database connection parameters
$DB_HOST = "localhost"
$DB_PORT = "3306"
$DB_NAME = "your_database_name"  # Replace with your actual database name
$DB_USER = "your_username"       # Replace with your username
$DB_PASSWORD = "your_password"   # Replace with your password

# MySQL command path (adjust if needed)
$MYSQL_PATH = "mysql"

Write-Host "=== MySQL Migration: Remove graded_by column ===" -ForegroundColor Green

# Step 1: Backup current state (optional but recommended)
Write-Host "`n1. Creating backup (optional)..." -ForegroundColor Yellow
$backup_file = "submissions_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"
Write-Host "Backup command (run manually if needed):" -ForegroundColor Cyan
Write-Host "mysqldump -h $DB_HOST -P $DB_PORT -u $DB_USER -p $DB_NAME submissions > $backup_file" -ForegroundColor White

# Step 2: Check current foreign keys
Write-Host "`n2. Checking current foreign key constraints..." -ForegroundColor Yellow
$check_fk_query = @"
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
    AND TABLE_SCHEMA = '$DB_NAME'
    AND REFERENCED_TABLE_NAME IS NOT NULL;
"@

Write-Host "Query to check foreign keys:" -ForegroundColor Cyan
Write-Host $check_fk_query -ForegroundColor White

# Step 3: Execute migration
Write-Host "`n3. Executing migration..." -ForegroundColor Yellow

$migration_sql = @"
-- Drop foreign key constraint
ALTER TABLE submissions DROP FOREIGN KEY FKmjfva3nkq8vlatkp2mk3678hh;

-- Drop the column
ALTER TABLE submissions DROP COLUMN graded_by;

-- Verify changes
DESCRIBE submissions;
"@

# Save migration to temp file
$temp_sql_file = "temp_migration.sql"
$migration_sql | Out-File -FilePath $temp_sql_file -Encoding UTF8

Write-Host "Migration SQL:" -ForegroundColor Cyan
Write-Host $migration_sql -ForegroundColor White

# Execute the migration
Write-Host "`nExecuting migration..." -ForegroundColor Yellow
try {
    # Method 1: Using mysql command line
    $mysql_command = "$MYSQL_PATH -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASSWORD $DB_NAME < $temp_sql_file"
    Write-Host "MySQL Command:" -ForegroundColor Cyan
    Write-Host $mysql_command -ForegroundColor White
    
    Write-Host "`nTo execute manually, run the following commands in MySQL:" -ForegroundColor Yellow
    Write-Host "1. Connect to MySQL:" -ForegroundColor Cyan
    Write-Host "   mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p $DB_NAME" -ForegroundColor White
    Write-Host "2. Run the migration:" -ForegroundColor Cyan
    Write-Host "   ALTER TABLE submissions DROP FOREIGN KEY FKmjfva3nkq8vlatkp2mk3678hh;" -ForegroundColor White
    Write-Host "   ALTER TABLE submissions DROP COLUMN graded_by;" -ForegroundColor White
    Write-Host "   DESCRIBE submissions;" -ForegroundColor White
}
catch {
    Write-Host "Error executing migration: $($_.Exception.Message)" -ForegroundColor Red
}
finally {
    # Clean up temp file
    if (Test-Path $temp_sql_file) {
        Remove-Item $temp_sql_file
    }
}

# Step 4: Verification
Write-Host "`n4. Verification steps..." -ForegroundColor Yellow
Write-Host "After running the migration, verify that:" -ForegroundColor Cyan
Write-Host "✓ graded_by column is removed from submissions table" -ForegroundColor White
Write-Host "✓ Foreign key constraint FKmjfva3nkq8vlatkp2mk3678hh is removed" -ForegroundColor White
Write-Host "✓ Application still compiles and runs correctly" -ForegroundColor White

Write-Host "`n5. Rollback plan (if needed)..." -ForegroundColor Yellow
$rollback_sql = @"
-- If you need to rollback, run these commands:
ALTER TABLE submissions ADD COLUMN graded_by BIGINT;
ALTER TABLE submissions ADD CONSTRAINT FKmjfva3nkq8vlatkp2mk3678hh 
    FOREIGN KEY (graded_by) REFERENCES users(id);

-- Update existing graded submissions
UPDATE submissions s 
SET graded_by = (
    SELECT c.teacher_id 
    FROM assignments a 
    JOIN courses c ON a.course_id = c.id 
    WHERE a.id = s.assignment_id
) 
WHERE s.graded_at IS NOT NULL;
"@

Write-Host "Rollback SQL:" -ForegroundColor Cyan
Write-Host $rollback_sql -ForegroundColor White

Write-Host "`n=== Migration Script Complete ===" -ForegroundColor Green
Write-Host "Please execute the migration commands manually in MySQL Workbench or command line." -ForegroundColor Yellow
