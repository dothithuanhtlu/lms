# Teacher Grading APIs - Complete Documentation

## Overview
This document describes the APIs available for teachers to manage grading and review student submissions in the LMS system.

## Authentication
All APIs require teacher authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

## API Endpoints

### 1. Get All Submissions by Assignment
**Endpoint:** `GET /api/submissions/assignment/{assignmentId}`
**Description:** Retrieve all student submissions for a specific assignment (teacher view)
**Access:** Teacher only

#### Parameters
- **Path Parameter:**
  - `assignmentId` (Long): ID of the assignment

- **Query Parameters:**
  - `page` (int, optional): Page number (default: 0)
  - `size` (int, optional): Page size (default: 20)

#### Request Example
```http
GET /api/submissions/assignment/1?page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response Example
```json
[
  {
    "id": 1,
    "assignmentId": 1,
    "studentId": 2,
    "studentName": "John Doe",
    "studentCode": "ST001",
    "content": "My assignment submission content",
    "status": "SUBMITTED",
    "score": null,
    "submittedAt": "2024-01-15T10:30:00",
    "gradedAt": null,
    "documents": [
      {
        "id": 1,
        "fileName": "assignment.pdf",
        "fileUrl": "https://res.cloudinary.com/.../assignment.pdf",
        "fileType": "application/pdf",
        "fileSize": 2048576
      }
    ]
  },
  {
    "id": 2,
    "assignmentId": 1,
    "studentId": 3,
    "studentName": "Jane Smith",
    "studentCode": "ST002",
    "content": "Another student's submission",
    "status": "LATE",
    "score": 85.5,
    "submittedAt": "2024-01-16T11:45:00",
    "gradedAt": "2024-01-17T14:20:00",
    "documents": []
  }
]
```

#### Response Fields
- `id`: Submission ID
- `assignmentId`: Assignment ID
- `studentId`: Student's user ID
- `studentName`: Student's full name
- `studentCode`: Student's code/username
- `content`: Text content of the submission
- `status`: Submission status (`SUBMITTED`, `LATE`, `NOT_SUBMITTED`)
- `score`: Grade score (null if not graded)
- `submittedAt`: Submission timestamp
- `gradedAt`: Grading timestamp (null if not graded)
- `documents`: Array of attached files

### 2. Grade a Submission
**Endpoint:** `PUT /api/submissions/{submissionId}/grade`
**Description:** Grade a student's submission
**Access:** Teacher only

#### Parameters
- **Path Parameter:**
  - `submissionId` (Long): ID of the submission to grade

#### Request Body
```json
{
  "score": 95.5,
  "feedback": "Excellent work! Well structured and comprehensive."
}
```

#### Request Example
```http
PUT /api/submissions/1/grade
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "score": 95.5,
  "feedback": "Excellent work! Well structured and comprehensive."
}
```

#### Response Example
```json
{
  "id": 1,
  "assignmentId": 1,
  "studentId": 2,
  "studentName": "John Doe",
  "studentCode": "ST001",
  "content": "My assignment submission content",
  "status": "SUBMITTED",
  "score": 95.5,
  "feedback": "Excellent work! Well structured and comprehensive.",
  "submittedAt": "2024-01-15T10:30:00",
  "gradedAt": "2024-01-17T15:30:00",
  "documents": [...]
}
```

### 3. Get Submission Statistics
**Endpoint:** `GET /api/submissions/assignment/{assignmentId}/statistics`
**Description:** Get statistical overview of submissions for an assignment
**Access:** Teacher only

#### Parameters
- **Path Parameter:**
  - `assignmentId` (Long): ID of the assignment

#### Request Example
```http
GET /api/submissions/assignment/1/statistics
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response Example
```json
{
  "assignmentId": 1,
  "totalStudents": 25,
  "totalSubmissions": 23,
  "submittedCount": 20,
  "lateSubmissions": 3,
  "notSubmittedCount": 2,
  "gradedCount": 15,
  "averageScore": 78.5,
  "maxScore": 98.0,
  "minScore": 45.0,
  "submissionRate": 92.0,
  "onTimeRate": 80.0
}
```

## Error Responses

### 404 Not Found
```json
{
  "error": "Assignment not found",
  "message": "Assignment with ID 999 does not exist",
  "timestamp": "2024-01-17T15:30:00"
}
```

### 403 Forbidden
```json
{
  "error": "Access denied",
  "message": "You don't have permission to access this assignment",
  "timestamp": "2024-01-17T15:30:00"
}
```

### 400 Bad Request
```json
{
  "error": "Invalid score",
  "message": "Score must be between 0 and 100",
  "timestamp": "2024-01-17T15:30:00"
}
```

## Usage Examples

### Example 1: Review All Submissions for Grading
```bash
# Get all submissions for assignment 1
curl -X GET "http://localhost:8080/api/submissions/assignment/1" \
  -H "Authorization: Bearer $TOKEN"

# Grade each submission
curl -X PUT "http://localhost:8080/api/submissions/123/grade" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"score": 88.5, "feedback": "Good work overall"}'
```

### Example 2: Get Statistics Before Grading
```bash
# Check submission statistics
curl -X GET "http://localhost:8080/api/submissions/assignment/1/statistics" \
  -H "Authorization: Bearer $TOKEN"
```

### Example 3: Paginate Through Large Number of Submissions
```bash
# Get first 5 submissions
curl -X GET "http://localhost:8080/api/submissions/assignment/1?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"

# Get next 5 submissions
curl -X GET "http://localhost:8080/api/submissions/assignment/1?page=1&size=5" \
  -H "Authorization: Bearer $TOKEN"
```

## Implementation Notes

1. **Pagination**: The API supports pagination to handle large numbers of submissions efficiently.

2. **Security**: Only teachers can access these endpoints. The system verifies that the authenticated user is the teacher of the course containing the assignment.

3. **Sorting**: Submissions are returned sorted by submission date (most recent first).

4. **File Access**: Document URLs include Cloudinary public URLs that can be accessed directly.

5. **Status Logic**: 
   - `SUBMITTED`: Submitted on time
   - `LATE`: Submitted after deadline
   - `NOT_SUBMITTED`: No submission found

6. **Grading**: Once graded, the `gradedAt` timestamp is set and `score`/`feedback` are populated.

## Testing
Use the provided PowerShell test script:
```powershell
.\test_get_submissions_by_assignment.ps1
```

This script tests:
- Teacher authentication
- Getting submissions with default pagination
- Getting submissions with custom pagination
- Handling invalid assignment IDs
- Response format validation
