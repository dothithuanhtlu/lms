# Cloudinary Integration Test Guide

## Test the File Upload Endpoints

### Prerequisites
1. Start the LMS application
2. Have a valid JWT token
3. Have test files ready (video, PDF, image)

### Test Cases

#### 1. Test Lesson Document Upload

```bash
# Upload a PDF document to lesson ID 1
curl -X POST \
  http://localhost:8080/api/lessons/1/documents/upload \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@test-document.pdf' \
  -F 'title=Course Material' \
  -F 'description=Important course reading material' \
  -F 'uploadedBy=teacher1'

# Expected Response:
{
    "success": true,
    "message": "File uploaded successfully",
    "document": {
        "id": 1,
        "title": "Course Material",
        "fileName": "test-document.pdf",
        "filePath": "https://res.cloudinary.com/...",
        "documentType": "PDF",
        "fileSize": 1024000,
        "mimeType": "application/pdf",
        "description": "Important course reading material",
        "isDownloadable": true,
        "createdAt": "2024-12-07T10:30:00",
        "uploadedBy": "teacher1",
        "lessonId": 1,
        "lessonTitle": "Introduction to Java"
    }
}
```

#### 2. Test Video Upload

```bash
# Upload a video file to lesson ID 1
curl -X POST \
  http://localhost:8080/api/lessons/1/documents/upload \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@lecture-video.mp4' \
  -F 'title=Lecture Recording' \
  -F 'description=Full lecture recording for week 1' \
  -F 'uploadedBy=teacher1'
```

#### 3. Test Get Lesson Documents

```bash
# Get all documents for lesson ID 1
curl -X GET \
  http://localhost:8080/api/lessons/1/documents \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

#### 4. Test Get Documents by Type

```bash
# Get only video documents
curl -X GET \
  http://localhost:8080/api/lessons/1/documents/type/VIDEO \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'

# Get only PDF documents
curl -X GET \
  http://localhost:8080/api/lessons/1/documents/type/PDF \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

#### 5. Test Video Thumbnail Generation

```bash
# Generate thumbnail at 30 seconds
curl -X GET \
  "http://localhost:8080/api/lessons/1/documents/1/thumbnail?timeInSeconds=30" \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

#### 6. Test Optimized URL Generation

```bash
# Generate optimized URL with specific dimensions
curl -X GET \
  "http://localhost:8080/api/lessons/1/documents/1/optimize?width=800&height=600" \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

#### 7. Test Document Count

```bash
# Count documents in lesson
curl -X GET \
  http://localhost:8080/api/lessons/1/documents/count \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

#### 8. Test Update Document Metadata

```bash
# Update document title and description
curl -X PUT \
  "http://localhost:8080/api/lessons/1/documents/1?title=Updated%20Title&description=Updated%20description&isDownloadable=false" \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

#### 9. Test Delete Document

```bash
# Delete document
curl -X DELETE \
  http://localhost:8080/api/lessons/1/documents/1 \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

#### 10. Test General File Upload

```bash
# Upload any file to general folder
curl -X POST \
  http://localhost:8080/api/upload/file \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@any-file.txt' \
  -F 'folder=documents'
```

#### 11. Test Image Upload with Optimization

```bash
# Upload image with optimization
curl -X POST \
  http://localhost:8080/api/upload/image \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@profile-pic.jpg' \
  -F 'folder=profiles' \
  -F 'width=200' \
  -F 'height=200' \
  -F 'quality=auto'
```

#### 12. Test Video Upload

```bash
# Upload video to general video folder
curl -X POST \
  http://localhost:8080/api/upload/video \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@promotional-video.mp4' \
  -F 'folder=promotions'
```

#### 13. Test Course Documents

```bash
# Get all documents for course ID 1
curl -X GET \
  http://localhost:8080/api/courses/1/documents \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### Error Test Cases

#### 1. Test File Size Limit

```bash
# Try uploading file larger than 100MB (should fail)
curl -X POST \
  http://localhost:8080/api/lessons/1/documents/upload \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@large-file.zip'

# Expected Response:
{
    "success": false,
    "error": "File size exceeds 100MB limit"
}
```

#### 2. Test Invalid File Type

```bash
# Try uploading unsupported file type (should fail)
curl -X POST \
  http://localhost:8080/api/lessons/1/documents/upload \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@malicious.exe'

# Expected Response:
{
    "success": false,
    "error": "File type not allowed: exe"
}
```

#### 3. Test Empty File

```bash
# Try uploading empty file (should fail)
curl -X POST \
  http://localhost:8080/api/lessons/1/documents/upload \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@empty-file.txt'

# Expected Response:
{
    "success": false,
    "error": "File is empty or null"
}
```

#### 4. Test Non-existent Lesson

```bash
# Try uploading to non-existent lesson (should fail)
curl -X POST \
  http://localhost:8080/api/lessons/99999/documents/upload \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@test-document.pdf'

# Expected Response:
{
    "success": false,
    "error": "Lesson not found with id: 99999"
}
```

### Frontend Testing with JavaScript

```html
<!DOCTYPE html>
<html>
<head>
    <title>Cloudinary Upload Test</title>
</head>
<body>
    <h2>Test File Upload</h2>
    
    <form id="uploadForm">
        <div>
            <label for="lessonId">Lesson ID:</label>
            <input type="number" id="lessonId" value="1" required>
        </div>
        
        <div>
            <label for="file">Select File:</label>
            <input type="file" id="file" required>
        </div>
        
        <div>
            <label for="title">Title:</label>
            <input type="text" id="title" placeholder="Document title">
        </div>
        
        <div>
            <label for="description">Description:</label>
            <textarea id="description" placeholder="Document description"></textarea>
        </div>
        
        <button type="submit">Upload File</button>
    </form>
    
    <div id="result"></div>
    
    <script>
        document.getElementById('uploadForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const formData = new FormData();
            const fileInput = document.getElementById('file');
            const lessonId = document.getElementById('lessonId').value;
            const title = document.getElementById('title').value;
            const description = document.getElementById('description').value;
            
            formData.append('file', fileInput.files[0]);
            if (title) formData.append('title', title);
            if (description) formData.append('description', description);
            formData.append('uploadedBy', 'test-user');
            
            try {
                const response = await fetch(`/api/lessons/${lessonId}/documents/upload`, {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'Authorization': 'Bearer YOUR_JWT_TOKEN_HERE'
                    }
                });
                
                const result = await response.json();
                document.getElementById('result').innerHTML = 
                    `<pre>${JSON.stringify(result, null, 2)}</pre>`;
                    
            } catch (error) {
                document.getElementById('result').innerHTML = 
                    `<p style="color: red;">Error: ${error.message}</p>`;
            }
        });
    </script>
</body>
</html>
```

### Expected File Structure in Cloudinary

After successful uploads, you should see files organized in Cloudinary like this:

```
lms/
  lessons/
    1/
      documents/
        xyz123_course-material.pdf
        abc456_lecture-video.mp4
    2/
      documents/
        def789_assignment.docx
  images/
    profile-pics/
      user123_avatar.jpg
  videos/
    lectures/
      intro-course.mp4
  general/
    miscellaneous-file.txt
```

### Monitoring Upload Success

Check the application logs for upload confirmations:
```
INFO  - File uploaded successfully to Cloudinary: lms/lessons/1/documents/xyz123_course-material
INFO  - Document uploaded successfully for lesson 1: course-material.pdf
```

### Troubleshooting Common Issues

1. **401 Unauthorized**: Check JWT token validity
2. **413 Payload Too Large**: File exceeds size limit
3. **400 Bad Request**: Invalid file type or empty file
4. **404 Not Found**: Lesson or document not found
5. **500 Internal Server Error**: Check Cloudinary configuration and logs
