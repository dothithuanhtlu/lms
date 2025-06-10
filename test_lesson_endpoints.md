# Lesson Management System - API Testing Guide

## Test the Lesson Management Endpoints

### 1. Get All Lessons for a Course
```http
GET /api/courses/{courseId}/lessons
```

### 2. Create a New Lesson
```http
POST /api/courses/{courseId}/lessons
Content-Type: application/json

{
  "title": "Introduction to Java Programming",
  "description": "Basic concepts of Java programming language",
  "content": "This lesson covers variables, data types, and basic syntax",
  "orderIndex": 1,
  "isPublished": true
}
```

### 3. Add Documents to a Lesson
```http
POST /api/lessons/{lessonId}/documents
Content-Type: application/json

{
  "title": "Java Basics Slides",
  "documentType": "PDF",
  "filePath": "/documents/java-basics.pdf",
  "fileSize": 2048576,
  "orderIndex": 1
}
```

### 4. Test Assignment Creation
```http
POST /api/courses/{courseId}/assignments
Content-Type: application/json

{
  "title": "Java Programming Quiz",
  "description": "Test your knowledge of Java basics",
  "assignmentType": "QUIZ",
  "totalMarks": 100,
  "dueDate": "2025-07-01",
  "isPublished": true
}
```

### 5. Add Questions to Assignment
```http
POST /api/assignments/{assignmentId}/questions
Content-Type: application/json

{
  "questionText": "What is the main method signature in Java?",
  "questionType": "MULTIPLE_CHOICE",
  "marks": 10,
  "orderIndex": 1,
  "options": [
    {
      "optionText": "public static void main(String[] args)",
      "isCorrect": true,
      "orderIndex": 1
    },
    {
      "optionText": "public void main(String[] args)",
      "isCorrect": false,
      "orderIndex": 2
    },
    {
      "optionText": "static void main(String[] args)",
      "isCorrect": false,
      "orderIndex": 3
    }
  ]
}
```

## Expected Responses

All endpoints should return appropriate HTTP status codes:
- `200 OK` for successful GET requests
- `201 CREATED` for successful POST requests
- `204 NO CONTENT` for successful PUT/DELETE requests
- `404 NOT FOUND` for non-existent resources
- `400 BAD REQUEST` for validation errors

## Dynamic currentStudents Calculation

The CourseDTO now automatically calculates currentStudents from enrollment count:
```java
this.currentStudents = course.getEnrollments() != null ? course.getEnrollments().size() : 0;
```

This ensures the count is always accurate and reflects the actual number of enrolled students.
