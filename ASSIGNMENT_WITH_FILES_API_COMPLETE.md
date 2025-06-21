# Assignment API với File Upload - Hoàn tất Implementation

## Tổng quan
Đã thành công implement API endpoint enhanced cho việc tạo assignment với file uploads trong một request duy nhất, tương tự như lesson API. Điều này giảm thiểu số lượng API calls cần thiết và cải thiện performance.

## 🎯 **API Endpoint mới đã tạo:**

```
POST /api/assignments/create-with-files
Content-Type: multipart/form-data
Authorization: Bearer {JWT_TOKEN}
```

## 🛠️ **Technical Implementation Details**

### **Files được tạo/sửa đổi:**

#### Files mới tạo:
1. **`CreateAssignmentWithFilesRequest.java`** - DTO cho multipart form data requests

#### Files được sửa đổi:
1. **`IAssignmentService.java`** - Added `createAssignmentWithFiles()` method
2. **`AssignmentService.java`** - Implemented logic xử lý file upload
3. **`AssignmentController.java`** - Added endpoint `/create-with-files`
4. **`AssignmentCreateDTO.java`** - Updated để phù hợp với pattern
5. **`AssignmentDTO.java`** - Updated response mapping

### **Service Layer Features:**
- ✅ **File Upload**: Upload multiple files to Cloudinary với folder structure `lms/assignments/{assignmentId}/`
- ✅ **Transaction Management**: Atomic operations cho assignment + file uploads
- ✅ **Error Handling**: Tiếp tục tạo assignment ngay cả khi một số files upload fail
- ✅ **Validation**: File size (max 100MB) và số lượng files (max 10)

### **Request Parameters:**

#### Required Parameters:
- `title` (String) - Assignment title
- `courseId` (Long) - ID của course
- `dueDate` (LocalDateTime) - Due date của assignment
- `maxScore` (Float) - Điểm tối đa

#### Optional Parameters:
- `description` (String) - Mô tả assignment
- `isPublished` (Boolean) - Trạng thái xuất bản (default: false)
- `allowLateSubmission` (Boolean) - Cho phép nộp muộn (default: false)
- `files` (List<MultipartFile>) - Files cần upload
- `documentsMetadata` (String) - JSON metadata cho documents (currently simplified)

## 🧪 **Test bằng Postman/Thunder Client:**

### **Method:** POST
### **URL:** `http://localhost:8080/api/assignments/create-with-files`

### **Headers:**
```
Authorization: Bearer your_jwt_token_here
Content-Type: multipart/form-data
```

### **Body (form-data):**
```
title: "Java Programming Assignment"
description: "Create a simple Java application"
maxScore: 100
dueDate: 2024-12-31T23:59:00
courseId: 1
isPublished: false
allowLateSubmission: true
files: [chọn file1.pdf, file2.docx, file3.zip]  (multiple files)
documentsMetadata: [leave empty for now - will use default values]
```

## 🖥️ **Test bằng cURL:**

```bash
curl -X POST http://localhost:8080/api/assignments/create-with-files \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "title=Java Programming Assignment" \
  -F "description=Create a simple Java application" \
  -F "maxScore=100" \
  -F "dueDate=2024-12-31T23:59:00" \
  -F "courseId=1" \
  -F "isPublished=false" \
  -F "allowLateSubmission=true" \
  -F "files=@assignment.pdf" \
  -F "files=@template.zip"
```

## 💻 **Test bằng JavaScript/Frontend:**

```javascript
const createAssignmentWithFiles = async (assignmentData, files) => {
    const formData = new FormData();
    
    // Basic assignment info
    formData.append('title', assignmentData.title);
    formData.append('description', assignmentData.description);
    formData.append('maxScore', assignmentData.maxScore);
    formData.append('dueDate', assignmentData.dueDate);
    formData.append('courseId', assignmentData.courseId);
    formData.append('isPublished', assignmentData.isPublished);
    formData.append('allowLateSubmission', assignmentData.allowLateSubmission);
    
    // Add files
    files.forEach((file) => {
        formData.append('files', file);
    });
    
    const response = await fetch('/api/assignments/create-with-files', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`
        },
        body: formData
    });
    
    return await response.json();
};

// Usage example
const assignmentData = {
    title: "Java Programming Assignment",
    description: "Create a simple Java application",
    maxScore: 100,
    dueDate: "2024-12-31T23:59:00",
    courseId: 1,
    isPublished: false,
    allowLateSubmission: true
};

const files = [selectedPdfFile, selectedZipFile];
const result = await createAssignmentWithFiles(assignmentData, files);
```

## 📋 **Expected Response:**

```json
{
    "id": 15,
    "title": "Java Programming Assignment",
    "description": "Create a simple Java application",
    "maxScore": 100.0,
    "dueDate": "2024-12-31T23:59:00",
    "courseId": 1,
    "courseCode": "CS101.2024.1.01",
    "createdAt": "2024-12-10T16:30:00",
    "updatedAt": "2024-12-10T16:30:00",
    "isPublished": false,
    "allowLateSubmission": true,
    "documents": [
        {
            "id": 25,
            "fileNameOriginal": "assignment.pdf",
            "filePath": "https://res.cloudinary.com/your-cloud/raw/upload/.../assignment.pdf"
        },
        {
            "id": 26,
            "fileNameOriginal": "template.zip",
            "filePath": "https://res.cloudinary.com/your-cloud/raw/upload/.../template.zip"
        }
    ]
}
```

## 🎯 **Lợi ích của API này:**

1. **Single Request**: Tạo assignment và upload files trong một call duy nhất
2. **Better UX**: Giảm loading times và network calls
3. **Atomic Operations**: All-or-nothing cho assignment creation
4. **Error Resilience**: Partial file upload failures không làm hỏng assignment creation
5. **Performance**: Giảm server round trips
6. **Consistency**: Tuân thủ pattern của lesson API

## ⚠️ **Lưu ý:**

1. **Authentication Required**: Cần JWT token hợp lệ
2. **Course Permission**: Chỉ users có quyền với course mới có thể tạo assignment
3. **File Size Limit**: Mỗi file tối đa 100MB
4. **File Count Limit**: Tối đa 10 files per assignment
5. **Cloudinary Integration**: Files được upload lên Cloudinary với folder structure rõ ràng
6. **Error Handling**: Nếu một file upload fail, các file khác vẫn tiếp tục upload

## 🔄 **So sánh với Lesson API:**

| Feature | Lesson API | Assignment API |
|---------|------------|----------------|
| Endpoint | `/api/lessons/create` | `/api/assignments/create-with-files` |
| File Upload | ✅ Multiple files | ✅ Multiple files |
| Metadata Support | ✅ Rich metadata | ⚠️ Simplified (due to AssignmentDocument schema) |
| Cloudinary Integration | ✅ Folder structure | ✅ Folder structure |
| File Types | ✅ Auto-detection | ⚠️ Basic support |
| Transaction Safety | ✅ Atomic | ✅ Atomic |

## 🚀 **Ready to Test!**

API đã sẵn sàng sử dụng. Bạn có thể test ngay với các cách trên!

## 🔮 **Future Enhancements:**

1. **Enhanced AssignmentDocument**: Thêm các fields như `fileSize`, `mimeType`, `description`, `createdAt`
2. **File Type Detection**: Tự động detect và categorize file types
3. **Advanced Metadata**: Support cho rich metadata như lesson
4. **File Versioning**: Support cho việc update existing files
5. **Bulk Operations**: Support cho tạo multiple assignments

---

**Status: ✅ COMPLETED & READY FOR TESTING**
