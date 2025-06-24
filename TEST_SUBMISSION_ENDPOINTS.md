# Test Submission Endpoints

## 1. Student Submit Assignment

```javascript
// Frontend example for submitting assignment
const submitAssignment = async (assignmentId, files) => {
    const formData = new FormData();
    
    // Add assignment data
    formData.append('assignmentId', assignmentId);
    
    // Add files if any
    if (files && files.length > 0) {
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }
    }
    
    try {
        const response = await fetch('/api/submissions/submit', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}` // JWT token
            },
            body: formData
        });
        
        if (response.ok) {
            const submission = await response.json();
            console.log('Assignment submitted successfully:', submission);
            return submission;
        } else {
            const error = await response.text();
            console.error('Submission failed:', error);
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error submitting assignment:', error);
        throw error;
    }
};

// Example usage
const files = document.getElementById('fileInput').files;
submitAssignment(123, files);
```

## 2. Teacher Grade Submission

```javascript
const gradeSubmission = async (submissionId, score, feedback) => {
    const gradeData = {
        score: score,
        feedback: feedback
    };
    
    try {
        const response = await fetch(`/api/submissions/${submissionId}/grade`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(gradeData)
        });
        
        if (response.ok) {
            const gradedSubmission = await response.json();
            console.log('Submission graded successfully:', gradedSubmission);
            return gradedSubmission;
        } else {
            const error = await response.text();
            console.error('Grading failed:', error);
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error grading submission:', error);
        throw error;
    }
};

// Example usage
gradeSubmission(456, 8.5, 'Good work! Minor improvements needed in algorithm optimization.');
```

## 3. Get Submissions by Assignment (for teachers)

```javascript
const getSubmissionsByAssignment = async (assignmentId, page = 0, size = 20) => {
    try {
        const response = await fetch(`/api/submissions/assignment/${assignmentId}?page=${page}&size=${size}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const submissions = await response.json();
            console.log('Submissions retrieved:', submissions);
            return submissions;
        } else {
            const error = await response.text();
            console.error('Failed to get submissions:', error);
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error getting submissions:', error);
        throw error;
    }
};
```

## 4. Get Student's Own Submissions

```javascript
const getMySubmissions = async (courseId = null, page = 0, size = 20) => {
    let url = `/api/submissions/my-submissions?page=${page}&size=${size}`;
    if (courseId) {
        url += `&courseId=${courseId}`;
    }
    
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const submissions = await response.json();
            console.log('My submissions:', submissions);
            return submissions;
        } else {
            const error = await response.text();
            console.error('Failed to get my submissions:', error);
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error getting my submissions:', error);
        throw error;
    }
};
```

## 5. Check if Student Has Submitted

```javascript
const checkSubmissionStatus = async (assignmentId) => {
    try {
        const response = await fetch(`/api/submissions/check/${assignmentId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const hasSubmitted = await response.json();
            console.log('Has submitted:', hasSubmitted);
            return hasSubmitted;
        } else {
            const error = await response.text();
            console.error('Failed to check submission status:', error);
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error checking submission status:', error);
        throw error;
    }
};
```

## 6. Update Submission (for students)

```javascript
const updateSubmission = async (submissionId, files) => {
    const formData = new FormData();
    
    // Add new files if any
    if (files && files.length > 0) {
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }
    }
    
    try {
        const response = await fetch(`/api/submissions/${submissionId}/update`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });
        
        if (response.ok) {
            const updatedSubmission = await response.json();
            console.log('Submission updated successfully:', updatedSubmission);
            return updatedSubmission;
        } else {
            const error = await response.text();
            console.error('Update failed:', error);
            throw new Error(error);
        }
    } catch (error) {
        console.error('Error updating submission:', error);
        throw error;
    }
};
```

## API Endpoints Summary

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| POST | `/api/submissions/submit` | Submit assignment | Student |
| PUT | `/api/submissions/{id}/update` | Update submission | Student |
| PUT | `/api/submissions/{id}/grade` | Grade submission | Teacher |
| GET | `/api/submissions/{id}` | Get submission by ID | Both |
| GET | `/api/submissions/assignment/{assignmentId}` | Get submissions by assignment | Teacher |
| GET | `/api/submissions/my-submissions` | Get student's submissions | Student |
| DELETE | `/api/submissions/{id}` | Delete submission | Student/Teacher |
| GET | `/api/submissions/check/{assignmentId}` | Check if submitted | Student |

## Response Format

### SubmissionResponse
```json
{
    "id": 123,
    "submittedAt": "2025-06-22T10:30:00",
    "gradedAt": "2025-06-22T15:45:00",
    "score": 8.5,
    "feedback": "Good work! Minor improvements needed.",
    "status": "GRADED",
    "isLate": false,
    "assignmentId": 456,
    "assignmentTitle": "Data Structures Assignment",
    "assignmentMaxScore": 10.0,
    "studentId": 789,
    "studentName": "John Doe",
    "studentEmail": "john.doe@example.com",
    "gradedById": 101,
    "gradedByName": "Prof. Smith",
    "documents": [
        {
            "id": 111,
            "fileName": "solution.pdf",
            "filePath": "https://cloudinary.com/...",
            "fileType": "application/pdf",
            "fileSize": 1024000,
            "isDownloadable": true
        }
    ]
}
```
