# Test API: Create Lesson with Files

## ✅ **IMPLEMENTATION STATUS: COMPLETE**
- All compilation errors fixed
- API endpoint fully implemented and ready for testing
- Build successful: `BUILD SUCCESSFUL in 42s`
- Method signature issues resolved
- Authentication and validation implemented

## 🎯 **API Endpoint mới đã tạo:**

```
POST /api/lessons/with-files
Content-Type: multipart/form-data
Authorization: Bearer {JWT_TOKEN}
```

## 🧪 **Test bằng Postman/Thunder Client:**

### **Method:** POST
### **URL:** `http://localhost:8080/api/lessons/with-files`

### **Headers:**
```
Authorization: Bearer your_jwt_token_here
Content-Type: multipart/form-data
```

### **Body (form-data):**
```
title: "Bài học JavaScript cơ bản"
description: "Giới thiệu về JavaScript từ đầu"
content: "Nội dung chi tiết về JavaScript..."
courseId: 1
lessonOrder: 1
durationMinutes: 90
isPublished: true
files: [chọn file1.pdf, video1.mp4, image1.jpg]  (multiple files)
fileDescriptions: ["Tài liệu PDF", "Video bài giảng", "Hình minh họa"]
fileDisplayOrders: [1, 2, 3]
fileIsPublic: [true, true, false]
```

## 🖥️ **Test bằng cURL:**

```bash
curl -X POST http://localhost:8080/api/lessons/with-files \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "title=JavaScript Basics" \
  -F "description=Introduction to JavaScript" \
  -F "content=Detailed JavaScript content..." \
  -F "courseId=1" \
  -F "lessonOrder=1" \
  -F "durationMinutes=90" \
  -F "isPublished=true" \
  -F "files=@document.pdf" \
  -F "files=@video.mp4" \
  -F "fileDescriptions=PDF Document" \
  -F "fileDescriptions=Video Lecture" \
  -F "fileDisplayOrders=1" \
  -F "fileDisplayOrders=2" \
  -F "fileIsPublic=true" \
  -F "fileIsPublic=true"
```

## 💻 **Test bằng JavaScript/Frontend:**

```javascript
const createLessonWithFiles = async (lessonData, files) => {
    const formData = new FormData();
    
    // Basic lesson info
    formData.append('title', lessonData.title);
    formData.append('description', lessonData.description);
    formData.append('content', lessonData.content);
    formData.append('courseId', lessonData.courseId);
    formData.append('lessonOrder', lessonData.lessonOrder);
    formData.append('durationMinutes', lessonData.durationMinutes);
    formData.append('isPublished', lessonData.isPublished);
    
    // Add files
    files.forEach((fileObj, index) => {
        formData.append('files', fileObj.file);
        formData.append('fileDescriptions', fileObj.description || '');
        formData.append('fileDisplayOrders', fileObj.displayOrder || index + 1);
        formData.append('fileIsPublic', fileObj.isPublic !== false);
    });
    
    const response = await fetch('/api/lessons/with-files', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`
        },
        body: formData
    });
    
    return await response.json();
};

// Usage example
const lessonData = {
    title: "JavaScript Fundamentals",
    description: "Learn JavaScript basics",
    content: "Detailed content here...",
    courseId: 1,
    lessonOrder: 1,
    durationMinutes: 120,
    isPublished: false
};

const files = [
    {
        file: selectedPdfFile,
        description: "Course material PDF",
        displayOrder: 1,
        isPublic: true
    },
    {
        file: selectedVideoFile,
        description: "Video lecture",
        displayOrder: 2,
        isPublic: true
    }
];

const result = await createLessonWithFiles(lessonData, files);
```

## 📋 **Expected Response:**

```json
{
    "id": 15,
    "title": "JavaScript Fundamentals",
    "description": "Learn JavaScript basics",
    "content": "Detailed content here...",
    "lessonOrder": 1,
    "durationMinutes": 120,
    "isPublished": false,
    "courseId": 1,
    "courseCode": "CS101.2024.1.01",
    "createdAt": "2024-12-10T16:30:00",
    "updatedAt": "2024-12-10T16:30:00",
    "documents": [
        {
            "id": 25,
            "fileName": "course-material.pdf",
            "fileType": "DOCUMENT",
            "fileUrl": "https://res.cloudinary.com/your-cloud/raw/upload/.../course-material.pdf",
            "fileSize": 2048576,
            "displayOrder": 1,
            "description": "Course material PDF",
            "isPublic": true,
            "uploadedAt": "2024-12-10T16:30:05"
        },
        {
            "id": 26,
            "fileName": "video-lecture.mp4",
            "fileType": "VIDEO",
            "fileUrl": "https://res.cloudinary.com/your-cloud/video/upload/.../video-lecture.mp4",
            "fileSize": 104857600,
            "displayOrder": 2,
            "description": "Video lecture",
            "isPublic": true,
            "uploadedAt": "2024-12-10T16:30:10"
        }
    ],
    "totalDocuments": 2,
    "totalAssignments": 0
}
```

## 🎯 **Lợi ích của API này:**

1. ✅ **One-shot Creation**: Tạo lesson + upload files trong 1 request
2. ✅ **Atomic Operation**: Nếu upload files fail, lesson vẫn được tạo
3. ✅ **Flexible File Metadata**: Có thể set description, order, public status cho từng file
4. ✅ **Progress Friendly**: Frontend có thể show progress của toàn bộ quá trình
5. ✅ **Error Handling**: Robust error handling cho từng file
6. ✅ **Transaction Safe**: Database operations được wrap trong @Transactional

## ⚠️ **Lưu ý:**

1. **Authentication Required**: Cần JWT token hợp lệ
2. **Teacher Permission**: Chỉ teacher sở hữu course mới có thể tạo lesson
3. **Lesson Order Unique**: lessonOrder phải unique trong cùng 1 course
4. **File Size Limit**: Mỗi file tối đa 100MB
5. **Error Handling**: Nếu 1 file upload fail, các file khác vẫn tiếp tục upload

## 🚀 **Ready to Test!**

API đã sẵn sàng sử dụng. Bạn có thể test ngay với các cách trên!
