# Cloudinary Integration Documentation

## Overview
Complete Cloudinary integration for the LMS system supporting file uploads for lessons, videos, documents, and other media files.

## Features Implemented

### 1. Core Cloudinary Service
- **File Upload**: Support for multiple file types (videos, documents, images, audio)
- **File Deletion**: Remove files from Cloudinary storage
- **URL Optimization**: Generate optimized URLs for file delivery
- **Video Thumbnails**: Generate thumbnails for video files
- **File Validation**: Size, type, and extension validation

### 2. Lesson Document Management
- **Upload Files to Lessons**: Attach documents, videos, images to lessons
- **Document Types**: Support for PDF, DOC, PPT, VIDEO, IMAGE, AUDIO, etc.
- **Metadata Management**: Title, description, downloadable status
- **Course-level Document Views**: Access all documents in a course

### 3. File Upload Controllers
- **Lesson Documents**: `/api/lessons/{lessonId}/documents/*`
- **General File Upload**: `/api/upload/*`
- **Specialized Uploads**: Image and video specific endpoints

## API Endpoints

### Lesson Document Management

#### Upload File to Lesson
```
POST /api/lessons/{lessonId}/documents/upload
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- title: String (optional)
- description: String (optional) 
- uploadedBy: String (optional, default: "system")
```

#### Get All Documents for Lesson
```
GET /api/lessons/{lessonId}/documents
```

#### Get Documents by Type
```
GET /api/lessons/{lessonId}/documents/type/{documentType}

Document Types: VIDEO, PDF, DOC, DOCX, PPT, PPTX, IMAGE, AUDIO, OTHER
```

#### Get Document by ID
```
GET /api/lessons/{lessonId}/documents/{documentId}
```

#### Delete Document
```
DELETE /api/lessons/{lessonId}/documents/{documentId}
```

#### Update Document Metadata
```
PUT /api/lessons/{lessonId}/documents/{documentId}

Parameters:
- title: String (optional)
- description: String (optional)
- isDownloadable: Boolean (optional)
```

#### Generate Optimized URL
```
GET /api/lessons/{lessonId}/documents/{documentId}/optimize

Parameters:
- width: Integer (optional)
- height: Integer (optional)
```

#### Generate Video Thumbnail
```
GET /api/lessons/{lessonId}/documents/{documentId}/thumbnail

Parameters:
- timeInSeconds: Integer (optional, default: 0)
```

#### Count Documents
```
GET /api/lessons/{lessonId}/documents/count
```

### Course Document Management

#### Get All Documents for Course
```
GET /api/courses/{courseId}/documents
```

### General File Upload

#### Upload Any File
```
POST /api/upload/file
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- folder: String (optional, default: "general")
```

#### Upload Image with Optimization
```
POST /api/upload/image
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- folder: String (optional, default: "images")
- width: Integer (optional)
- height: Integer (optional)
- quality: String (optional)
```

#### Upload Video
```
POST /api/upload/video
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- folder: String (optional, default: "videos")
```

## Configuration

### Application Properties
```properties
# Cloudinary Configuration
cloudinary.cloud-name=your-cloud-name
cloudinary.api-key=your-api-key
cloudinary.api-secret=your-api-secret

# File Upload Configuration
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
spring.servlet.multipart.enabled=true
spring.servlet.multipart.location=${java.io.tmpdir}
```

### Supported File Types
- **Documents**: PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, TXT
- **Images**: JPG, JPEG, PNG, GIF, BMP, SVG, WEBP
- **Videos**: MP4, AVI, MOV, WMV, FLV, WEBM, MKV, M4V
- **Audio**: MP3, WAV, FLAC, AAC, OGG

### File Size Limits
- Maximum file size: 100MB
- Maximum request size: 100MB

## Usage Examples

### Frontend JavaScript Example

#### Upload File to Lesson
```javascript
const uploadFileToLesson = async (lessonId, file, title, description) => {
    const formData = new FormData();
    formData.append('file', file);
    if (title) formData.append('title', title);
    if (description) formData.append('description', description);
    formData.append('uploadedBy', 'teacher-username');

    const response = await fetch(`/api/lessons/${lessonId}/documents/upload`, {
        method: 'POST',
        body: formData,
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    return await response.json();
};
```

#### Get Lesson Documents
```javascript
const getLessonDocuments = async (lessonId) => {
    const response = await fetch(`/api/lessons/${lessonId}/documents`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    
    return await response.json();
};
```

#### Get Video Thumbnail
```javascript
const getVideoThumbnail = async (lessonId, documentId, timeInSeconds = 0) => {
    const response = await fetch(
        `/api/lessons/${lessonId}/documents/${documentId}/thumbnail?timeInSeconds=${timeInSeconds}`, 
        {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        }
    );
    
    return await response.text(); // Returns thumbnail URL
};
```

### cURL Examples

#### Upload Video File
```bash
curl -X POST \
  http://localhost:8080/api/lessons/1/documents/upload \
  -H 'Authorization: Bearer your-jwt-token' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@/path/to/video.mp4' \
  -F 'title=Introduction Video' \
  -F 'description=Course introduction and overview' \
  -F 'uploadedBy=teacher1'
```

#### Get Documents by Type
```bash
curl -X GET \
  http://localhost:8080/api/lessons/1/documents/type/VIDEO \
  -H 'Authorization: Bearer your-jwt-token'
```

## File Organization in Cloudinary

Files are organized in Cloudinary using the following folder structure:
- Lesson documents: `lms/lessons/{lessonId}/documents/`
- General uploads: `lms/general/`
- Images: `lms/images/`
- Videos: `lms/videos/`

## Error Handling

The API returns standardized error responses:

### Success Response
```json
{
    "success": true,
    "message": "File uploaded successfully",
    "document": {
        "id": 1,
        "title": "Introduction Video",
        "fileName": "intro.mp4",
        "filePath": "https://res.cloudinary.com/...",
        "documentType": "VIDEO",
        "fileSize": 52428800,
        "mimeType": "video/mp4",
        "description": "Course introduction",
        "isDownloadable": true,
        "createdAt": "2024-12-07T10:30:00",
        "uploadedBy": "teacher1",
        "lessonId": 1,
        "lessonTitle": "Introduction to Java"
    }
}
```

### Error Response
```json
{
    "success": false,
    "error": "File size exceeds 100MB limit"
}
```

## Security Considerations

1. **File Validation**: All files are validated for type, size, and content
2. **Authentication**: All endpoints require proper JWT authentication
3. **Authorization**: Access control based on user roles and course enrollment
4. **File Scanning**: Consider implementing virus scanning for uploaded files
5. **Rate Limiting**: Implement rate limiting for upload endpoints

## Performance Optimizations

1. **CDN Delivery**: Cloudinary provides global CDN for fast file delivery
2. **Auto Format**: Automatically selects best format for browsers
3. **Lazy Loading**: Implement lazy loading for video and image content
4. **Thumbnail Generation**: Pre-generate thumbnails for videos and images
5. **Compression**: Automatic compression and optimization

## Monitoring and Analytics

1. **Upload Metrics**: Track upload success/failure rates
2. **File Usage**: Monitor which files are accessed most frequently
3. **Storage Usage**: Track storage consumption per course/lesson
4. **Performance**: Monitor upload and delivery times

## Future Enhancements

1. **Batch Upload**: Support for multiple file uploads
2. **Progress Tracking**: Real-time upload progress
3. **File Versioning**: Support for file version management
4. **Advanced Analytics**: Detailed usage and engagement metrics
5. **AI Features**: Automatic transcription, content analysis
