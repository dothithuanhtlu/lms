# Unsubmitted Assignment Count API Documentation

## Overview
New API endpoint to get the count of unsubmitted assignments for a specific student. This is useful for tracking student progress and identifying students who may need additional support.

## API Endpoint

### GET `/api/submissions/student/{studentId}/unsubmitted-count`

**Purpose**: Get the total count of assignments that a student has not yet submitted.

**Method**: `GET`

**Path Parameter**:
- `studentId` (Long, required): The ID of the student

**Authentication**: Required (Bearer token)

**Authorization**: 
- Teachers can access data for any student
- Students can access their own data (implementation dependent on security setup)

## Request Example

```bash
GET /api/submissions/student/123/unsubmitted-count
Authorization: Bearer <token>
```

## Response Format

### Success Response (200 OK)
```json
{
  "studentId": 123,
  "unsubmittedCount": 5,
  "message": "Student has 5 unsubmitted assignments"
}
```

### Error Response (404 Not Found)
```json
{
  "error": "Student not found with id: 123"
}
```

### Error Response (500 Internal Server Error)
```json
{
  "error": "Failed to get unsubmitted assignment count: <error details>"
}
```

## Business Logic

### What is Counted
The API counts assignments that meet ALL of the following criteria:
1. **Published**: Assignment must be published (`isPublished = true`)
2. **Enrolled**: Student must be enrolled in the course containing the assignment
3. **Not Submitted**: Student has not submitted the assignment yet

### What is NOT Counted
- Unpublished assignments
- Assignments from courses the student is not enrolled in
- Assignments the student has already submitted (regardless of grade)
- Draft assignments

### SQL Logic
The count is calculated using this query:
```sql
SELECT COUNT(a) 
FROM Assignment a 
JOIN a.course c 
JOIN c.enrollments e 
WHERE e.student.id = :studentId 
AND a.isPublished = true 
AND NOT EXISTS (
  SELECT s FROM Submission s 
  WHERE s.assignment.id = a.id 
  AND s.student.id = :studentId
)
```

## Use Cases

### 1. Teacher Dashboard
- Monitor which students have pending assignments
- Identify students who may need academic support
- Track class-wide assignment completion rates

### 2. Student Dashboard
- Show students their current workload
- Help students prioritize which assignments to complete
- Provide visibility into pending work

### 3. Academic Advisor Portal
- Track student progress across multiple courses
- Identify students at risk of falling behind
- Support academic intervention strategies

### 4. Parent Portal
- Give parents visibility into their child's academic workload
- Help parents support their child's time management
- Enable family discussions about academic priorities

## Technical Implementation

### Files Modified
1. **ISubmissionService.java**
   - Added method: `getUnsubmittedAssignmentCountByStudentId(Long studentId)`

2. **SubmissionService.java**
   - Implemented the service method with validation and logging

3. **SubmissionRepository.java**
   - Added query: `countUnsubmittedAssignmentsByStudentId(Long studentId)`

4. **SubmissionController.java**
   - Added endpoint: `GET /student/{studentId}/unsubmitted-count`

### Performance Considerations
- **Efficient Query**: Uses JPA joins to minimize database round trips
- **Indexed Lookups**: Leverages existing indexes on student enrollments
- **Single Query**: Returns count in one database operation
- **Validation**: Includes student existence check for better error handling

### Error Handling
- **Student Not Found**: Returns 404 with clear error message
- **Database Errors**: Returns 500 with logged error details
- **Authentication**: Returns 401 if token is invalid
- **Authorization**: Returns 403 if user lacks permission (security dependent)

## Example Usage Scenarios

### Scenario 1: Teacher Checking Student Progress
```bash
# Teacher wants to see how many assignments Student ID 5 hasn't completed
GET /api/submissions/student/5/unsubmitted-count
Authorization: Bearer teacher_token

Response:
{
  "studentId": 5,
  "unsubmittedCount": 3,
  "message": "Student has 3 unsubmitted assignments"
}
```

### Scenario 2: Student Dashboard
```bash
# Student checking their own pending work
GET /api/submissions/student/15/unsubmitted-count
Authorization: Bearer student_token

Response:
{
  "studentId": 15,
  "unsubmittedCount": 0,
  "message": "Student has 0 unsubmitted assignments"
}
```

### Scenario 3: Academic Advisor Review
```bash
# Advisor checking multiple students
GET /api/submissions/student/25/unsubmitted-count
Authorization: Bearer advisor_token

Response:
{
  "studentId": 25,
  "unsubmittedCount": 8,
  "message": "Student has 8 unsubmitted assignments"
}
```

## Integration with Existing APIs

### Related Endpoints
- `GET /api/submissions/my-submissions` - Get student's submitted assignments
- `GET /api/submissions/assignment/{assignmentId}/statistics` - Get assignment statistics
- `GET /api/submissions/assignment/{assignmentId}` - Get all submissions for an assignment

### Data Consistency
The unsubmitted count is calculated in real-time, so it will always reflect:
- Current enrollment status
- Current assignment publication status
- Current submission status

## Testing

### Test Script
Use `test_unsubmitted_count_api.ps1` to test the API:
- Tests with teacher authentication
- Tests with student authentication
- Tests error cases (invalid student ID)
- Compares with existing submission data

### Manual Testing Steps
1. Create a student and enroll in courses
2. Create some assignments (published and unpublished)
3. Submit some assignments, leave others unsubmitted
4. Call the API and verify the count matches expectations
5. Test with different user roles to verify authorization

## Future Enhancements

### Potential Improvements
1. **Course Filtering**: Add optional courseId parameter to filter by specific course
2. **Due Date Filtering**: Add options to count only overdue assignments
3. **Priority Levels**: Include assignment importance/priority in the count
4. **Caching**: Add caching for frequently accessed student data
5. **Bulk Operations**: Allow getting counts for multiple students at once

### Related Features
- **Assignment Difficulty**: Weight assignments by difficulty/points
- **Time Estimates**: Include estimated completion time for pending work
- **Deadline Proximity**: Prioritize assignments by approaching due dates
- **Course Load Balance**: Show distribution of work across courses

## Security Considerations

### Access Control
- Validate that requesting user has permission to view student data
- Consider implementing role-based access (teacher, advisor, student, parent)
- Log access attempts for audit purposes

### Data Privacy
- Ensure compliance with educational data privacy regulations
- Consider masking or limiting data based on user role
- Implement proper authentication and authorization checks

## Monitoring and Logging

### Log Messages
The API generates the following log entries:
- **INFO**: "Getting unsubmitted assignment count for student ID: {studentId}"
- **INFO**: "Found {count} unsubmitted assignments for student: {name} ({code})"
- **ERROR**: Database or validation errors with full stack traces

### Metrics to Track
- API response times
- Most frequently queried students
- Average unsubmitted counts across institution
- Error rates and types
