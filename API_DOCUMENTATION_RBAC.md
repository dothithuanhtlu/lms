# API Documentation - Role-Based Access Control

## Quick Summary by Role

### ADMIN
- **GET** `/admin/users`, `/admin/users/statistics`, `/admin/courses/info`, `/admin/courses/{id}/details`, `/admin/student/{code}`, `/admin/teacher/{code}`, `/admin/admin/{code}`, `/admin/subjects/{id}`, `/admin/departments/names`, `/admin/classrooms/name`, `/admin/majors/{id}/allresponse`, `/admin/majors/{id}/subjects`, `/admin/subjects/{id}/courses`, `/admin/department/{id}/teachers_select`, `/admin/department/{id}/name`, `/departments/allresponses`, `/subjects/{id}/courses`
- **POST** `/admin/users`, `/admin/courses`, `/admin/user`, `/enrollments`
- **PUT** `/admin/users/{id}`, `/admin/courses/{id}`, `/admin/user/{id}`, `/admin/courses/{id}/students/{id}/scores`
- **DELETE** `/admin/courses/{id}`, `/admin/user/{id}`, `/enrollments`

### TEACHER
- **GET** `/admin/courses/byteacher/{id}`, `/admin/courses/{id}/full-details`, `/api/lessons/{id}`, `/api/assignments/detail/{id}`, `/api/submissions/assignment/{id}`, `/admin/courses/{id}/students/{id}/scores`
- **POST** `/api/lessons/create`, `/api/assignments/create-with-files`, `/api/submissions/{id}/grade`, `/api/lesson-documents/upload`
- **PUT** `/api/lessons/{id}`, `/api/assignments/{id}/publish`, `/api/lessons/{id}/publish`, `/api/lessons/{id}/update-with-files`, `/api/assignments/update/{id}`, `/api/assignments/{id}/update-with-files`
- **DELETE** `/api/assignments/delete/{id}`

### STUDENT
- **GET** `/admin/courses/bystudent/{id}`, `/admin/student/courses/{id}/details`, `/api/lessons/{id}`, `/api/assignments/detail/{id}`, `/api/submissions/student/{id}/unsubmitted-count`
- **POST** `/api/submissions/submit-or-update`

### SHARED (All Roles)
- **GET** `/chatbot/{message}`, `/auth/account`, `/health`
- **POST** `/login`

---

## Overview
This document provides a comprehensive list of all APIs used in the LMS system, organized by role for authorization and permission management.

## Base URL
All APIs are hosted at: `http://localhost:8080`

---

## 1. Authentication & Authorization APIs

### Login System
- **POST** `/login` - User authentication
  - **Used by**: All roles (login page)
  - **Purpose**: Authenticate user and return JWT token
  - **Required**: Username, password
  - **Returns**: User info, access token, role

### Token Management
- **Authorization Header**: `Bearer {accessToken}`
- **Used by**: All authenticated requests
- **Storage**: localStorage ('accessToken', 'user')

---

## 2. ADMIN ROLE APIs

### User Management
- **GET** `/admin/users?current=1&pageSize=1000` - Get all users with pagination
- **POST** `/admin/users` - Create new user
- **PUT** `/admin/users/{userId}` - Update user
- **DELETE** `/admin/user/{userId}` - Delete user
- **GET** `/admin/users/statistics` - Get user statistics

### User Details by Role
- **GET** `/admin/student/{userCode}` - Get student details
- **GET** `/admin/teacher/{userCode}` - Get teacher details  
- **GET** `/admin/admin/{userCode}` - Get admin details

### Course Management
- **GET** `/admin/courses/info` - Get course statistics
- **GET** `/admin/courses` - Get all courses
- **POST** `/admin/courses` - Create new course
- **PUT** `/admin/courses/{courseId}` - Update course
- **DELETE** `/admin/courses/{courseId}` - Delete course
- **GET** `/admin/courses/{courseId}/details` - Get course details
- **GET** `/admin/courses/{courseId}/full-details` - Get full course details
- **GET** `/admin/courses/{courseId}/students/{studentId}/scores` - Update student scores

### Subject & Department Management
- **GET** `/admin/subjects/{subjectId}` - Get subject details
- **GET** `/admin/subjects/{subjectId}/courses` - Get courses by subject
- **GET** `/admin/majors/{departmentId}/allresponse` - Get majors by department
- **GET** `/admin/majors/{majorId}/subjects` - Get subjects by major
- **GET** `/admin/department/{departmentId}/teachers_select` - Get teachers for selection
- **GET** `/admin/department/{departmentId}/name` - Get department name
- **GET** `/admin/teacher/{teacherCode}` - Get teacher info by code

### Classroom Management
- **GET** `/admin/classrooms/name` - Get classroom names
- **GET** `/admin/departments/names` - Get department names

### Student Course Access
- **GET** `/admin/student/courses/{courseId}/details` - Get course details for student
- **GET** `/admin/courses/bystudent/{studentId}` - Get courses by student
- **GET** `/admin/courses/byteacher/{teacherId}` - Get courses by teacher

---

## 3. TEACHER ROLE APIs

### Course Management
- **GET** `/admin/courses/byteacher/{teacherId}` - Get teacher's courses
- **GET** `/admin/courses/{courseId}/full-details` - Get full course details
- **POST** `/admin/courses/{courseId}/students/{studentId}/scores` - Update student scores

### Lesson Management
- **POST** `/api/lessons/create` - Create new lesson
- **GET** `/api/lessons/{lessonId}` - Get lesson details
- **PUT** `/api/lessons/{lessonId}` - Update lesson
- **PUT** `/api/lessons/{lessonId}/update-with-files` - Update lesson with files
- **PUT** `/api/lessons/{lessonId}/publish?isPublished=true` - Publish lesson
- **POST** `/api/lessons/{lessonId}/documents/upload` - Upload lesson documents

### Assignment Management
- **POST** `/api/assignments/create-with-files` - Create assignment with files
- **GET** `/api/assignments/detail/{assignmentId}` - Get assignment details
- **PUT** `/api/assignments/update/{assignmentId}` - Update assignment
- **PUT** `/api/assignments/{assignmentId}/update-with-files` - Update assignment with files
- **PUT** `/api/assignments/{assignmentId}/publish?isPublished=true` - Publish assignment
- **DELETE** `/api/assignments/delete/{assignmentId}` - Delete assignment

### Grading System
- **GET** `/api/submissions/assignment/{assignmentId}?current={page}&pageSize={size}` - Get assignment submissions
- **POST** `/api/submissions/{submissionId}/grade` - Grade submission

---

## 4. STUDENT ROLE APIs

### Course Access
- **GET** `/admin/courses/bystudent/{studentId}` - Get student's enrolled courses
- **GET** `/admin/student/courses/{courseId}/details` - Get course details

### Lesson Access
- **GET** `/api/lessons/{lessonId}` - Get lesson content

### Assignment & Submission
- **GET** `/api/assignments/detail/{assignmentId}` - Get assignment details
- **POST** `/api/submissions/submit-or-update` - Submit or update assignment
- **GET** `/api/submissions/student/{studentId}/unsubmitted-count` - Get unsubmitted count

---

## 5. SHARED/PUBLIC APIs

### Department & Subject Data
- **GET** `/departments/allresponses` - Get all departments
- **GET** `/subjects/{subjectId}/courses` - Get courses by subject (fallback)

### Enrollment Management
- **POST** `/enrollments` - Create enrollment
- **DELETE** `/enrollments` - Remove enrollment

### Document Management
- **POST** `/api/lesson-documents/upload` - Upload lesson documents
- **GET** `/api/lesson-documents/test-cloudinary-status` - Test Cloudinary status
- **GET** `/api/lesson-documents/check-usage` - Check usage statistics

### Chatbot System
- **GET** `/chatbot/{encodedMessage}` - Get chatbot response

---

## 6. File Access Patterns

### Static File Access
- **GET** `/{cleanPath}` - Access uploaded files (videos, documents)
- **Pattern**: `http://localhost:8080/{relativePath}`

### File Upload Endpoints
- **POST** `/api/lessons/create` - With multipart form data
- **POST** `/api/assignments/create-with-files` - With multipart form data
- **POST** `/api/submissions/submit-or-update` - With multipart form data

---

## 7. Role-Based File Access

### Admin Dashboard Access
- **Files**: `admin-dashboard.html`, `managerusers.html`, `admincourses.html`
- **Sub-pages**: `addcourse_admin.html`, `updateCourse.html`, `detailcourse.html`

### Teacher Dashboard Access
- **Files**: `teacher-dashboard.html`, `teacher-course-detail.html`
- **Sub-pages**: `edit-lesson.html`, `grade-assignment.html`, `edit-assignment.html`

### Student Dashboard Access
- **Files**: `student-dashboard.html`, `student-course-detail.html`
- **Sub-pages**: `lesson-detail.html`, `assignment-detail.html`, `submit-assignment.html`

---

## 8. Security Patterns

### Authentication Flow
1. Login via `/login` endpoint
2. Store JWT token in localStorage
3. Include `Authorization: Bearer {token}` header in all requests
4. Redirect to role-specific dashboard

### Role-Based Redirection
- **Admin**: `admin-dashboard.html`
- **Teacher**: `teacher-dashboard.html`
- **Student**: `student-dashboard.html`

### API Access Control
- **Admin APIs**: `/admin/*` - Full system access
- **Teacher APIs**: `/admin/courses/*` (limited) + `/api/lessons/*` + `/api/assignments/*`
- **Student APIs**: `/admin/student/*` + `/api/lessons/{id}` + `/api/assignments/{id}` + `/api/submissions/*`

---

## 9. HTTP Methods Summary

### GET (Read Operations)
- User data retrieval
- Course information
- Lesson content
- Assignment details
- Statistics and reports

### POST (Create Operations)
- User creation
- Course creation
- Lesson creation
- Assignment creation
- File submissions

### PUT (Update Operations)
- User updates
- Course updates
- Lesson updates
- Assignment updates
- Score updates

### DELETE (Delete Operations)
- User deletion
- Course deletion
- Assignment deletion
- Enrollment removal

---

## 10. Error Handling

### Standard Response Format
```json
{
    "statusCode": 200,
    "message": "Success",
    "data": {}
}
```

### Common Status Codes
- **200**: Success
- **201**: Created
- **204**: No Content (successful deletion)
- **400**: Bad Request
- **401**: Unauthorized
- **403**: Forbidden
- **404**: Not Found
- **500**: Internal Server Error

---

## 11. Authorization Matrix

| API Group | Admin | Teacher | Student | Public |
|-----------|-------|---------|---------|---------|
| `/admin/users/*` | ✅ | ❌ | ❌ | ❌ |
| `/admin/courses/*` | ✅ | ✅ (limited) | ❌ | ❌ |
| `/admin/student/*` | ✅ | ❌ | ✅ (own data) | ❌ |
| `/admin/teacher/*` | ✅ | ✅ (own data) | ❌ | ❌ |
| `/api/lessons/*` | ✅ | ✅ | ✅ (read-only) | ❌ |
| `/api/assignments/*` | ✅ | ✅ | ✅ (limited) | ❌ |
| `/api/submissions/*` | ✅ | ✅ (grading) | ✅ (own) | ❌ |
| `/departments/*` | ✅ | ✅ | ✅ | ❌ |
| `/subjects/*` | ✅ | ✅ | ✅ | ❌ |
| `/enrollments` | ✅ | ❌ | ❌ | ❌ |
| `/chatbot/*` | ✅ | ✅ | ✅ | ❌ |
| `/login` | ✅ | ✅ | ✅ | ✅ |

---

## 12. Implementation Notes

### Frontend Files by Role
- **Admin**: Uses iframes to embed `managerusers.html` and `admincourses.html`
- **Teacher**: Direct access to teacher-specific pages
- **Student**: Direct access to student-specific pages

### Token Storage
- **Key**: `accessToken` in localStorage
- **User Data**: `user` object in localStorage
- **Expiration**: Handled by backend, frontend redirects to login on 401

### File Upload Handling
- **Multipart Form Data**: Used for file uploads
- **Cloudinary Integration**: For media file storage
- **Local Storage**: For document files

This documentation provides a complete overview of the API structure for implementing proper role-based access control in the LMS system.
