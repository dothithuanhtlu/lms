# 📚 Assignment Update APIs - Frontend Integration Guide

## 🎯 **2 APIs Update Assignment**

### 1️⃣ **API Update Assignment Info (Chỉ sửa thông tin)**

#### **Endpoint**
```http
PUT /api/assignments/update/{assignmentId}
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}
```

#### **Parameters**
- `assignmentId` (Path): ID của assignment cần update

#### **Form Data Fields** (AssignmentUpdateDTO)
```javascript
// Required fields
title: string (required, not blank)
dueDate: string (required, ISO format: "2025-07-01T23:59:59")

// Optional fields  
description: string
maxScore: number (float)
isPublished: boolean
allowLateSubmission: boolean
courseId: number (long)
fileUploadsNew: File[] (multipart files to add)
fileDeleteIds: number[] (IDs of files to delete)
```

#### **Frontend Call Example (JavaScript)**
```javascript
async function updateAssignmentInfo(assignmentId, data) {
    const formData = new FormData();
    
    // Required fields
    formData.append('title', data.title);
    formData.append('dueDate', data.dueDate); // "2025-07-01T23:59:59"
    
    // Optional fields
    if (data.description) formData.append('description', data.description);
    if (data.maxScore) formData.append('maxScore', data.maxScore);
    if (data.isPublished !== undefined) formData.append('isPublished', data.isPublished);
    if (data.allowLateSubmission !== undefined) formData.append('allowLateSubmission', data.allowLateSubmission);
    if (data.courseId) formData.append('courseId', data.courseId);
    
    // Add new files (if any)
    if (data.newFiles && data.newFiles.length > 0) {
        data.newFiles.forEach(file => {
            formData.append('fileUploadsNew', file);
        });
    }
    
    // Delete existing files (if any)
    if (data.deleteFileIds && data.deleteFileIds.length > 0) {
        data.deleteFileIds.forEach(id => {
            formData.append('fileDeleteIds', id);
        });
    }
    
    const response = await fetch(`/api/assignments/update/${assignmentId}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`
            // Don't set Content-Type, browser will set it automatically for FormData
        },
        body: formData
    });
    
    return await response.json();
}
```

#### **Usage Example**
```javascript
const updateData = {
    title: "Updated Assignment Title",
    description: "New description",
    maxScore: 100,
    dueDate: "2025-08-15T23:59:59",
    isPublished: true,
    allowLateSubmission: false,
    newFiles: [file1, file2], // File objects from input
    deleteFileIds: [123, 456] // IDs of files to remove
};

const result = await updateAssignmentInfo(1, updateData);
```

---

### 2️⃣ **API Update Assignment With Files (Thay thế toàn bộ files)**

#### **Endpoint**
```http
PUT /api/assignments/{assignmentId}/update-with-files
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}
```

#### **Parameters**
- `assignmentId` (Path): ID của assignment cần update

#### **Form Data Fields** (UpdateAssignmentWithFilesRequest)
```javascript
// All fields are optional
title: string (max 200 characters)
description: string
maxScore: number (min 0)
dueDate: string (ISO format: "2025-07-01T23:59:59")
allowLateSubmission: boolean
isPublished: boolean
documentsMetadata: string (JSON string)
files: File[] (multipart files - will replace ALL existing files)
```

#### **Frontend Call Example (JavaScript)**
```javascript
async function updateAssignmentWithFiles(assignmentId, data) {
    const formData = new FormData();
    
    // Basic info (all optional)
    if (data.title) formData.append('title', data.title);
    if (data.description) formData.append('description', data.description);
    if (data.maxScore !== undefined) formData.append('maxScore', data.maxScore);
    if (data.dueDate) formData.append('dueDate', data.dueDate);
    if (data.allowLateSubmission !== undefined) formData.append('allowLateSubmission', data.allowLateSubmission);
    if (data.isPublished !== undefined) formData.append('isPublished', data.isPublished);
    
    // Documents metadata (JSON string)
    if (data.documentsMetadata) {
        const metadataJson = JSON.stringify(data.documentsMetadata);
        formData.append('documentsMetadata', metadataJson);
    }
    
    // Files (will replace ALL existing files)
    if (data.files && data.files.length > 0) {
        data.files.forEach(file => {
            formData.append('files', file);
        });
    }
    
    const response = await fetch(`/api/assignments/${assignmentId}/update-with-files`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`
            // Don't set Content-Type, browser will set it automatically for FormData
        },
        body: formData
    });
    
    return await response.json();
}
```

#### **Documents Metadata Format**
```javascript
const documentsMetadata = [
    {
        title: "Assignment Requirements",
        documentType: "DOCUMENT", // DOCUMENT, REFERENCE, TEMPLATE, OTHER
        isDownloadable: true
    },
    {
        title: "Sample Solution", 
        documentType: "REFERENCE",
        isDownloadable: false
    }
];
```

#### **Usage Example**
```javascript
const updateData = {
    title: "Completely Updated Assignment",
    description: "New description with new files",
    maxScore: 95,
    dueDate: "2025-09-01T23:59:59",
    isPublished: true,
    allowLateSubmission: true,
    files: [newFile1, newFile2, newFile3], // These will REPLACE all existing files
    documentsMetadata: [
        { title: "New Requirements", documentType: "DOCUMENT", isDownloadable: true },
        { title: "New Template", documentType: "TEMPLATE", isDownloadable: true }
    ]
};

const result = await updateAssignmentWithFiles(1, updateData);
```

---

## 🔄 **Key Differences**

| Aspect | Update Info | Update With Files |
|--------|-------------|-------------------|
| **URL** | `/update/{id}` | `/{id}/update-with-files` |
| **File Behavior** | Add/Remove specific files | Replace ALL files |
| **DTO** | `AssignmentUpdateDTO` | `UpdateAssignmentWithFilesRequest` |
| **Title Required** | ✅ Yes | ❌ No (optional) |
| **DueDate Required** | ✅ Yes | ❌ No (optional) |
| **File Management** | Incremental (add/delete) | Complete replacement |

---

## 💡 **When to Use Which API**

### **Use Update Info (`/update/{id}`) when:**
- ✅ Chỉ muốn sửa thông tin cơ bản
- ✅ Muốn thêm vài files mới mà giữ files cũ
- ✅ Muốn xóa một số files cụ thể
- ✅ Muốn quản lý files từng cái một

### **Use Update With Files (`/update-with-files`) when:**
- ✅ Muốn thay thế TOÀN BỘ files
- ✅ Muốn "refresh" tất cả documents
- ✅ Có bộ files hoàn toàn mới
- ✅ Muốn đảm bảo không có files cũ nào còn lại

---

## 🚨 **Important Notes**

### **File Validation**
- ✅ Maximum 10 files per request
- ✅ Maximum 100MB per file
- ✅ All file types supported

### **Authentication**
- ✅ JWT token required in Authorization header
- ✅ Teacher role required
- ✅ Must own the assignment (course ownership)

### **Error Handling**
```javascript
try {
    const result = await updateAssignmentInfo(assignmentId, data);
    console.log('Success:', result);
} catch (error) {
    if (error.status === 404) {
        console.error('Assignment not found');
    } else if (error.status === 400) {
        console.error('Validation error:', error.message);
    } else if (error.status === 401) {
        console.error('Authentication required');
    } else {
        console.error('Server error:', error.message);
    }
}
```

### **Response Format**
Both APIs return the same response format:
```javascript
{
    id: 123,
    title: "Updated Assignment",
    description: "Updated description",
    maxScore: 100.0,
    dueDate: "2025-08-15T23:59:59",
    allowLateSubmission: true,
    isPublished: true,
    documents: [
        {
            id: 456,
            fileNameOriginal: "requirements.pdf",
            filePath: "https://res.cloudinary.com/...",
            fileSize: 1024000
        }
    ],
    createdAt: "2025-06-25T10:00:00",
    updatedAt: "2025-06-25T11:30:00"
}
```

---

## 🎯 **Quick Reference**

### **Update Info Only**
```javascript
// For incremental changes
PUT /api/assignments/update/{id}
FormData: title*, dueDate*, description, maxScore, files, deleteIds...
```

### **Update With Complete File Replacement**
```javascript
// For complete refresh
PUT /api/assignments/{id}/update-with-files  
FormData: title, description, maxScore, dueDate, files, metadata...
```

**Choose the right API based on your frontend's file management strategy!** 🚀
