# Student Submittable Unsubmitted Assignment Count API

## Overview
This API provides the count of assignments that a student can still submit. It only includes assignments that are either not yet due or overdue but allow late submission.

## Endpoint
```
GET /api/submissions/student/{studentId}/unsubmitted-count
```

## Logic
The API counts assignments that meet ALL of the following criteria:
- ✅ Student is enrolled in the assignment's course
- ✅ Assignment is published (`isPublished = true`)
- ✅ Student hasn't submitted the assignment yet
- ✅ Assignment is either:
  - **Not due yet** (`dueDate > currentTime`), OR
  - **Overdue but allows late submission** (`dueDate <= currentTime AND allowLateSubmission = true`)

## Exclusions
The API excludes assignments that are:
- ❌ Not published
- ❌ Already submitted by the student
- ❌ Overdue and don't allow late submission

## Use Cases

### For Students
- **Dashboard**: Show actionable pending assignments
- **Study Planning**: Focus on assignments that can still be completed
- **Time Management**: Prioritize recoverable work

### For Teachers
- **Progress Monitoring**: See which assignments students can still complete
- **Intervention**: Identify students who can still catch up
- **Course Management**: Track recoverable vs. missed assignments

### For Academic Advisors
- **Student Support**: Focus counseling on recoverable assignments
- **Performance Tracking**: Monitor actionable workload
- **Intervention Planning**: Identify where students can still improve

## Request

### Path Parameters
- `studentId` (Long, required): ID of the student

### Headers
- `Authorization: Bearer {token}` (required)

### Example Request
```http
GET /api/submissions/student/123/unsubmitted-count
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Response

### Success Response (200 OK)
```json
{
  "studentId": 123,
  "unsubmittedCount": 5,
  "message": "Student has 5 assignments that can still be submitted"
}
```

### Response Fields
- `studentId` (Long): ID of the student
- `unsubmittedCount` (Long): Number of assignments that can still be submitted
- `message` (String): Descriptive message about the count

### Error Responses

#### Student Not Found (404)
```json
{
  "error": "Student not found with id: 123"
}
```

#### Authentication Required (401)
```json
{
  "error": "Authentication required"
}
```

#### Server Error (500)
```json
{
  "error": "Failed to get unsubmitted assignment count: Internal server error"
}
```

## Database Query Logic

The API uses the following SQL logic:
```sql
SELECT COUNT(a) FROM Assignment a 
JOIN a.course.enrollments e 
WHERE e.student.id = :studentId 
AND a.isPublished = true 
AND NOT EXISTS (
    SELECT s FROM Submission s 
    WHERE s.assignment.id = a.id 
    AND s.student.id = :studentId
) 
AND (
    a.dueDate > :currentTime 
    OR (a.dueDate <= :currentTime AND a.allowLateSubmission = true)
)
```

## Example Scenarios

### Scenario 1: Mixed Assignment States
- Assignment A: Due tomorrow (not submitted) → **COUNTED** ✅
- Assignment B: Due yesterday, allows late submission (not submitted) → **COUNTED** ✅  
- Assignment C: Due yesterday, no late submission (not submitted) → **NOT COUNTED** ❌
- Assignment D: Due next week (already submitted) → **NOT COUNTED** ❌

**Result**: `unsubmittedCount = 2`

### Scenario 2: All Assignments Recoverable
- Assignment A: Due in 3 days (not submitted) → **COUNTED** ✅
- Assignment B: Due yesterday, allows late submission (not submitted) → **COUNTED** ✅
- Assignment C: Due last week, allows late submission (not submitted) → **COUNTED** ✅

**Result**: `unsubmittedCount = 3`

### Scenario 3: No Recoverable Assignments
- Assignment A: Due yesterday, no late submission (not submitted) → **NOT COUNTED** ❌
- Assignment B: Due next week (already submitted) → **NOT COUNTED** ❌
- Assignment C: Due last month, no late submission (not submitted) → **NOT COUNTED** ❌

**Result**: `unsubmittedCount = 0`

## Security Considerations

### Authorization
- Requires valid authentication token
- Teachers can access any student's count
- Students can access their own count (implementation dependent)
- Admin can access any student's count

### Data Privacy
- Only returns count, not sensitive assignment details
- Respects course enrollment boundaries
- Only includes published assignments

## Performance Considerations

### Optimization
- Uses efficient JPA query with joins
- Leverages database indexes on:
  - `student_id` in enrollments
  - `assignment_id` and `student_id` in submissions
  - `is_published` in assignments
  - `due_date` in assignments

### Caching Recommendations
- Cache results for 5-10 minutes per student
- Invalidate cache when:
  - Student submits an assignment
  - Assignment due dates change
  - Assignment publication status changes
  - Student enrollment changes

## Integration Examples

### Frontend Dashboard
```javascript
// Get submittable assignment count for student dashboard
const response = await fetch('/api/submissions/student/123/unsubmitted-count', {
  headers: { 'Authorization': `Bearer ${token}` }
});

const data = await response.json();
console.log(`${data.unsubmittedCount} assignments can still be submitted`);
```

### Teacher Interface
```javascript
// Check which students have recoverable assignments
for (const student of students) {
  const response = await fetch(`/api/submissions/student/${student.id}/unsubmitted-count`);
  const data = await response.json();
  
  if (data.unsubmittedCount > 0) {
    console.log(`${student.name} can still submit ${data.unsubmittedCount} assignments`);
  }
}
```

## Testing

### Test Script
Use `test_unsubmitted_count_api.ps1` to test the API:

```powershell
# Update the script variables
$STUDENT_ID = 123  # Target student ID
$BASE_URL = "http://localhost:8080"

# Run the test
.\test_unsubmitted_count_api.ps1
```

### Test Cases
1. **Valid student with recoverable assignments**
2. **Valid student with no recoverable assignments**  
3. **Student with mixed assignment states**
4. **Invalid student ID**
5. **Unauthenticated request**

## API Versioning
- Current version: v1
- Endpoint path: `/api/submissions/student/{studentId}/unsubmitted-count`
- Breaking changes will require new version endpoint
