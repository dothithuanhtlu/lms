# 🔧 Fixed: Assignment Creation với File Upload

## ✅ **Đã sửa các lỗi:**

### 1. **Lỗi fileDisplayOrders conversion:**
- **Trước:** `Integer[] fileDisplayOrders` 
- **Sau:** `String fileDisplayOrders` (nhận JSON string từ FE)

### 2. **Cloudinary folder structure:**
- **Trước:** `assignments/{id}/`
- **Sau:** `lms/assignments/{id}/`

## 📁 **Cloudinary Folder Structure (FIXED):**

```
📁 lms/
  📁 assignments/
    📁 1/
      📄 image1.jpg
      📄 document.pdf
    📁 2/  
      📄 video.mp4
      📄 presentation.pptx
```

## 🧪 **Request Format từ Frontend:**

### **FormData cần gửi:**
```javascript
const formData = new FormData();

// Basic fields
formData.append('title', 'Bài tập về Java');
formData.append('description', 'Mô tả bài tập');
formData.append('courseId', '1');
formData.append('dueDate', '2024-12-31T23:59:00');
formData.append('maxScore', '100');
formData.append('allowLateSubmission', 'true');

// Files
formData.append('files', file1); // Image file
formData.append('files', file2); // PDF file

// File metadata (FIXED - không cần Integer array)
formData.append('fileDescriptions', JSON.stringify(['Hình ảnh minh họa', 'Tài liệu hướng dẫn']));
formData.append('fileDisplayOrders', '[1, 2]'); // JSON string, không phải array
```

### **Postman/Thunder Client test:**
```
POST http://localhost:8080/api/assignments/create-with-files
Content-Type: multipart/form-data

Body (form-data):
title: "Java Programming Assignment" 
description: "Create a simple Java application"
courseId: 1
dueDate: 2024-12-31T23:59:00  
maxScore: 100
allowLateSubmission: true
files: [select image.jpg]
files: [select document.pdf]
fileDisplayOrders: "[1, 2]"
```

## 🎯 **Expected Cloudinary URLs:**

Khi upload thành công, files sẽ có URLs như:
```
https://res.cloudinary.com/your-cloud/image/upload/v123/lms/assignments/1/image.jpg
https://res.cloudinary.com/your-cloud/raw/upload/v123/lms/assignments/1/document.pdf
```

## 🔍 **Debug Logs được thêm:**

1. **Controller validation:**
   ```
   Validating file: image.jpg (size: 15360 bytes, type: image/jpeg)
   Request validation: title=Java Assignment, courseId=1, files count=1
   ```

2. **Service processing:**
   ```
   Processing 1 files for assignment ID: 1
   Uploading 1 files to Cloudinary folder: lms/assignments/1
   Processing file 1: image.jpg, size: 15360 bytes
   ✅ File uploaded successfully to Cloudinary: https://res.cloudinary.com/.../image.jpg
   ✅ Document saved to database: image.jpg for assignment ID: 1
   ```

## 🚀 **Test ngay:**

1. **Tạo assignment với image file** - Sẽ upload vào `lms/assignments/{id}/`
2. **Check Cloudinary dashboard** - Verify folder structure đúng
3. **Check response** - Có URL Cloudinary trong documents array

## ⚠️ **Lưu ý cho Frontend:**

1. `fileDisplayOrders` gửi dưới dạng JSON string: `"[1, 2]"`
2. Không gửi dưới dạng array trực tiếp
3. Files sẽ được lưu trong folder `lms/assignments/{assignmentId}/`

**Bây giờ tạo assignment với file ảnh sẽ không còn lỗi!** ✅
