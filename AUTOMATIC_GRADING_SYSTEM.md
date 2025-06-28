# Automatic Grading System Documentation

## Overview
The LMS now features a fully automated grading system that automatically assigns a score of 0 to students who fail to submit assignments before the deadline (when late submission is not allowed). This eliminates the need for manual intervention from teachers.

## Key Features

### 1. Automatic Zero Grading
- **Trigger**: Automatic scheduled task every 30 minutes
- **Condition**: Assignment is overdue AND late submission is not allowed
- **Action**: Creates submission record with score 0 for students who haven't submitted
- **No Manual Intervention Required**: Teachers no longer need to call any API

### 2. Scheduling Configuration
- **Frequency**: Every 30 minutes (1,800,000 milliseconds)
- **Implementation**: Spring's `@Scheduled` annotation
- **Service**: `AutoGradingService`
- **Enabled**: Automatically with `@EnableScheduling` in main application class

### 3. Eligibility Criteria
An assignment is eligible for auto-grading when ALL conditions are met:
- Assignment has a due date
- Current time is past the due date (assignment is overdue)
- Assignment does not allow late submission (`allowLateSubmission = false`)
- Assignment is published (`isPublished = true`)

## Technical Implementation

### Components
1. **AutoGradingService**: Main service handling auto-grading logic
2. **AssignmentRepository**: Added method to find expired assignments
3. **SubmissionRepository**: Used to check existing submissions and create new ones
4. **UserRepository**: Used to get all students in a course

### Key Methods

#### AutoGradingService
```java
@Scheduled(fixedRate = 1800000) // Every 30 minutes
public void autoGradeExpiredAssignments()
```
- Main scheduled method that processes all expired assignments

```java
public int processExpiredAssignment(Assignment assignment)
```
- Processes a single assignment and creates zero submissions for non-submitters

```java
public int checkAndAutoGradeAssignment(Long assignmentId)
```
- Manual trigger for specific assignment (used by other services if needed)

#### AssignmentRepository
```java
List<Assignment> findExpiredAssignmentsForAutoGrading(LocalDateTime currentTime)
```
- Finds all assignments eligible for auto-grading

### Database Impact
- Creates new `Submission` records for students who haven't submitted
- Sets score to 0.0f
- Marks as submitted with automatic feedback
- Records grading time as current timestamp
- Uses assignment due date as submission time

## Changes Made

### Removed Components
1. **Manual API Endpoint**: Removed `POST /api/submissions/assignment/{assignmentId}/auto-grade-overdue`
2. **Service Method**: Removed `autoGradeOverdueSubmissions` from `ISubmissionService` and `SubmissionService`
3. **Controller Method**: Removed auto-grade endpoint from `SubmissionController`

### Added Components
1. **AutoGradingService**: New service with scheduled auto-grading
2. **@EnableScheduling**: Added to main application class
3. **Repository Method**: Added `findExpiredAssignmentsForAutoGrading` to `AssignmentRepository`

## Monitoring and Logging

### Log Messages
- **🤖** Starting scheduled auto-grading
- **🎯** Found X expired assignments to process
- **✅** Auto-graded X students for assignment
- **🎉** Scheduled auto-grading completed
- **❌** Error messages for failed auto-grading attempts

### Log Levels
- **INFO**: Main process flow and results
- **DEBUG**: Detailed processing information
- **WARN**: Warnings for missing data
- **ERROR**: Error conditions

## Configuration Options

### Scheduling Interval
Current: 30 minutes (1,800,000 ms)
To change: Modify the `fixedRate` parameter in `@Scheduled` annotation

### Enable/Disable Auto-Grading
To disable: Remove `@EnableScheduling` from main application class
To enable: Add `@EnableScheduling` to main application class

## Benefits

1. **No Manual Intervention**: Teachers don't need to remember to grade overdue assignments
2. **Consistent Grading**: All overdue assignments are processed automatically
3. **Timely Processing**: Runs every 30 minutes ensuring prompt grading
4. **Audit Trail**: All auto-graded submissions are logged and tracked
5. **Fair Grading**: Ensures consistent application of late submission policies

## Example Workflow

1. **Assignment Creation**: Teacher creates assignment with due date and `allowLateSubmission = false`
2. **Student Submission Period**: Students submit assignments before due date
3. **Due Date Passes**: Assignment becomes overdue
4. **Auto-Grading Trigger**: Within 30 minutes, scheduled task runs
5. **Processing**: System finds all students who haven't submitted
6. **Zero Assignment**: Creates submission records with score 0 for non-submitters
7. **Completion**: Teachers can see all submissions (including auto-graded zeros) in grading interface

## Database Schema Impact

### Submission Table
New auto-graded submissions will have:
- `score`: 0.0
- `feedback`: "Automatic grade: Assignment not submitted by due date"
- `status`: SUBMITTED
- `submitted_at`: Assignment due date
- `graded_at`: Current timestamp when auto-graded
- `is_late`: false (uses due date as submission time)

## API Changes

### Removed Endpoints
- `POST /api/submissions/assignment/{assignmentId}/auto-grade-overdue`

### Existing Endpoints (Unchanged)
- `GET /api/submissions/assignment/{assignmentId}` - Teachers can still view all submissions including auto-graded ones
- `GET /api/submissions/assignment/{assignmentId}/statistics` - Statistics include auto-graded submissions

## Testing

The auto-grading system can be tested by:
1. Creating an assignment with past due date and `allowLateSubmission = false`
2. Enrolling students who don't submit
3. Waiting for the next scheduled run (max 30 minutes)
4. Checking the submissions list to see auto-generated zero submissions

## Future Enhancements

Potential improvements:
1. Configurable scheduling interval via application properties
2. Email notifications to teachers when auto-grading occurs
3. Dashboard for monitoring auto-grading statistics
4. Option to exclude specific students from auto-grading
5. Bulk revert functionality for incorrectly auto-graded submissions
