# Unified Submission API Documentation

## Overview

The new unified submission API simplifies the frontend by providing a single endpoint that automatically handles both creating new submissions and updating existing ones.

## API Endpoint

```
POST /api/submissions/submit-or-update
```

**Content-Type:** `multipart/form-data`

## Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `assignmentId` | Long | Yes | ID of the assignment |
| `files` | MultipartFile[] | No | Files to submit |
| `documentsMetadata` | String | No | JSON metadata for documents |

## Response

Returns `SubmissionResponse` object with submission details.

## Behavior Logic

### 1. Check Existing Submission
The API first checks if the authenticated student already has a submission for the given assignment.

### 2. Create New (First Submission)
If **no existing submission** is found:
- Creates a new submission record
- Uploads provided files to Cloudinary
- Sets submission timestamp to current time
- Returns new submission details

### 3. Update Existing (Subsequent Submissions)
If **existing submission** is found:
- **Completely deletes all old files** (from both Cloudinary and database)
- **Uploads new files** provided in the request
- **Updates submission timestamp** to current time
- **Keeps same submission ID**
- Returns updated submission details

## Security & Validation

✅ **Authentication Required:** Must be logged in student
✅ **Authorization:** Can only submit to assignments in enrolled courses
✅ **Ownership:** Can only update own submissions
✅ **Grading Protection:** Cannot update already graded submissions (`score != null`)
✅ **Assignment Validation:** Assignment must exist and be published

## Status Logic

The submission status follows the simplified 3-status system:
- **NOT_SUBMITTED:** No submission exists (only for display, not actual records)
- **SUBMITTED:** Submission made on time or before due date
- **LATE:** Submission made after due date

## Frontend Usage

### Single Form Approach
```javascript
// Frontend only needs one form and one API call
const submitAssignment = async (assignmentId, files, metadata) => {
    const formData = new FormData();
    formData.append('assignmentId', assignmentId);
    formData.append('documentsMetadata', JSON.stringify(metadata));
    
    files.forEach(file => {
        formData.append('files', file);
    });
    
    const response = await fetch('/api/submissions/submit-or-update', {
        method: 'POST',
        body: formData,
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    
    return response.json();
};
```

### No Need to Check Submission Status
The frontend doesn't need to:
- Check if submission already exists
- Determine whether to call create or update API
- Handle different endpoints for different scenarios

Just call the same API every time!

## Example Request

```bash
curl -X POST "http://localhost:8080/api/submissions/submit-or-update" \
  -H "Authorization: Bearer your-jwt-token" \
  -F "assignmentId=21" \
  -F "files=@document.pdf" \
  -F 'documentsMetadata={"documents":[{"description":"My assignment","displayOrder":1}]}'
```

## Response Examples

### First Submission (Create)
```json
{
  "id": 123,
  "assignmentId": 21,
  "studentId": 15,
  "submittedAt": "2025-06-27T14:30:00",
  "status": "SUBMITTED",
  "score": null,
  "feedback": null,
  "isLate": false,
  "documents": [
    {
      "id": 456,
      "fileName": "document.pdf",
      "filePath": "https://cloudinary.com/...",
      "description": "My assignment"
    }
  ]
}
```

### Update Submission (Replace)
```json
{
  "id": 123,  // Same ID as before
  "assignmentId": 21,
  "studentId": 15,
  "submittedAt": "2025-06-27T15:45:00",  // Updated timestamp
  "status": "SUBMITTED",
  "score": null,
  "feedback": null,
  "isLate": false,
  "documents": [
    {
      "id": 789,  // New document ID (old one was deleted)
      "fileName": "updated_document.pdf",
      "filePath": "https://cloudinary.com/...",
      "description": "My updated assignment"
    }
  ]
}
```

## Benefits

### For Frontend Developers
1. **Single API endpoint** to remember
2. **No complex logic** to determine create vs update
3. **Consistent behavior** regardless of submission state
4. **Simpler error handling**

### For Students
1. **Seamless experience** - same action replaces previous submission
2. **No confusion** about create vs update operations
3. **File replacement** clearly understood
4. **Consistent timestamps** show when submission was last updated

### For System
1. **Clean file management** - old files automatically cleaned up
2. **Consistent data** - no orphaned files or submissions
3. **Audit trail** maintained through submission timestamps
4. **Database integrity** preserved

## Migration from Existing APIs

The new API complements existing endpoints:

- **Keep:** `POST /api/submissions/submit` (for specific create operations)
- **Keep:** `PUT /api/submissions/{id}/update` (for specific update operations)  
- **New:** `POST /api/submissions/submit-or-update` (for unified frontend usage)

Frontends can migrate to the unified endpoint while keeping backward compatibility.
