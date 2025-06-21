# Test API: Create Assignment with Files

## ✅ **IMPLEMENTATION STATUS: COMPLETE**
- All compilation errors fixed
- API endpoint fully implemented and ready for testing
- Build successful
- Method signature aligned with lesson pattern
- File upload logic implemented with Cloudinary integration

## 🎯 **API Endpoint đã tạo:**

```
POST /api/assignments/create-with-files
Content-Type: multipart/form-data
Authorization: Bearer {JWT_TOKEN}
```

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
title: "Data Structures Assignment"
description: "Implement basic data structures in Java"
maxScore: 100
dueDate: 2024-12-25T23:59:00
courseId: 1
isPublished: false
allowLateSubmission: true
files: [chọn assignment.pdf, template.zip, readme.txt]  (multiple files)
```

## 🖥️ **Test bằng cURL:**

```bash
curl -X POST http://localhost:8080/api/assignments/create-with-files \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "title=Data Structures Assignment" \
  -F "description=Implement basic data structures in Java" \
  -F "maxScore=100" \
  -F "dueDate=2024-12-25T23:59:00" \
  -F "courseId=1" \
  -F "isPublished=false" \
  -F "allowLateSubmission=true" \
  -F "files=@assignment.pdf" \
  -F "files=@template.zip" \
  -F "files=@readme.txt"
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
    title: "Data Structures Assignment",
    description: "Implement basic data structures in Java",
    maxScore: 100,
    dueDate: "2024-12-25T23:59:00",
    courseId: 1,
    isPublished: false,
    allowLateSubmission: true
};

const files = [selectedPdfFile, selectedZipFile, selectedTxtFile];
const result = await createAssignmentWithFiles(assignmentData, files);
console.log('Assignment created:', result);
```

## 📋 **Expected Response:**

```json
{
    "id": 20,
    "title": "Data Structures Assignment",
    "description": "Implement basic data structures in Java",
    "maxScore": 100.0,
    "dueDate": "2024-12-25T23:59:00",
    "courseId": 1,
    "courseCode": "CS101.2024.1.01",
    "createdAt": "2024-12-10T18:00:00",
    "updatedAt": "2024-12-10T18:00:00",
    "isPublished": false,
    "allowLateSubmission": true,
    "documents": [
        {
            "id": 30,
            "fileNameOriginal": "assignment.pdf",
            "filePath": "https://res.cloudinary.com/your-cloud/raw/upload/v123/lms/assignments/20/assignment.pdf"
        },
        {
            "id": 31,
            "fileNameOriginal": "template.zip",
            "filePath": "https://res.cloudinary.com/your-cloud/raw/upload/v123/lms/assignments/20/template.zip"
        },
        {
            "id": 32,
            "fileNameOriginal": "readme.txt", 
            "filePath": "https://res.cloudinary.com/your-cloud/raw/upload/v123/lms/assignments/20/readme.txt"
        }
    ]
}
```

## 🎯 **Key Features Implemented:**

1. **✅ File Upload to Cloudinary**: Files upload to `lms/assignments/{assignmentId}/` folder
2. **✅ Multiple Files Support**: Accept multiple files in single request
3. **✅ File Validation**: 
   - Max file size: 100MB per file
   - Max files count: 10 files per assignment
4. **✅ Transaction Safety**: Assignment creation + file upload in atomic transaction
5. **✅ Error Handling**: Graceful handling of partial file upload failures
6. **✅ Response Consistency**: Same structure as lesson API

## 🔍 **Cloudinary File Structure:**

Files được upload với structure:
```
lms/
  assignments/
    {assignmentId}/
      assignment.pdf
      template.zip
      readme.txt
```

## ⚠️ **Lưu ý khi Test:**

1. **Authentication Required**: Cần JWT token hợp lệ từ teacher/admin
2. **Course Access**: User phải có quyền truy cập course được chỉ định
3. **File Format**: Hỗ trợ tất cả file formats (PDF, DOC, ZIP, TXT, etc.)
4. **File Size**: Maximum 100MB per file
5. **Concurrent Uploads**: Files upload parallel để tối ưu performance
6. **Error Recovery**: Nếu một file fail, assignment vẫn được tạo với các files thành công

## 🚀 **Test Scenarios:**

### **Scenario 1: Basic Assignment với Files**
- Title: "Java Basics Assignment"
- 2-3 files: PDF instructions, ZIP template, TXT readme
- Expected: Success với tất cả files upload

### **Scenario 2: Assignment không có Files**
- Chỉ assignment data, không attach files
- Expected: Assignment được tạo thành công

### **Scenario 3: Large Files Test**
- Upload files gần 100MB limit
- Expected: Success nếu trong limit, error nếu vượt

### **Scenario 4: Too Many Files**
- Upload > 10 files
- Expected: Validation error

### **Scenario 5: Invalid Course ID**
- CourseId không tồn tại
- Expected: 404 Course not found

## 🔧 **Debug Tips:**

1. **Check Logs**: Monitor console logs cho upload progress
2. **Cloudinary Dashboard**: Verify files appear in correct folder
3. **Database**: Check assignment and assignment_documents tables
4. **Network Tab**: Monitor request/response in browser dev tools

## 🚀 **Ready to Test!**

API đã sẵn sàng sử dụng. Test ngay với các scenarios trên để verify functionality!

---

**Status: ✅ READY FOR TESTING**
