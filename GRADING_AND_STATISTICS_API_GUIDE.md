# 📊 TÀI LIỆU API CHẤM ĐIỂM VÀ THỐNG KÊ SUBMISSION

## 📋 **TỔNG QUAN**

Tài liệu này mô tả chi tiết các API để chấm điểm submission và xem thống kê, bao gồm:
- API chấm điểm cho giáo viên
- API thống kê submission theo assignment
- Validation và error handling
- Test scenarios và examples

---

## 🎯 **1. API CHẤM ĐIỂM SUBMISSION**

### **1.1. PUT /api/submissions/{submissionId}/grade**

**Mô tả:** Chấm điểm cho một submission (chỉ dành cho giáo viên)

**Method:** `PUT`
**URL:** `/api/submissions/{submissionId}/grade`
**Authentication:** Required (Bearer Token)
**Role:** Teacher only

#### **Request Body:**
```json
{
  "submissionId": 123,
  "score": 85.5,
  "feedback": "Bài làm tốt, logic rõ ràng. Cần cải thiện comment code."
}
```

#### **Validation Rules:**
- `submissionId`: Required, must exist
- `score`: 
  - Optional (có thể null để chỉ cập nhật feedback)
  - Min: 0.0
  - Max: Assignment max score hoặc 999.99
  - Decimal precision: 2 chữ số sau dấu phẩy
- `feedback`: Optional, text có thể dài

#### **Response Success (200):**
```json
{
  "id": 123,
  "submittedAt": "2025-01-15T14:30:00",
  "gradedAt": "2025-01-16T10:30:00",
  "score": 85.5,
  "feedback": "Bài làm tốt, logic rõ ràng. Cần cải thiện comment code.",
  "status": "GRADED",
  "isLate": false,
  "assignmentId": 1,
  "assignmentTitle": "Bài tập lập trình Java",
  "assignmentMaxScore": 100.0,
  "studentId": 456,
  "studentName": "Nguyễn Văn A",
  "studentEmail": "student@example.com",
  "gradedById": 789,
  "gradedByName": "Nguyễn Thị B",
  "documents": [
    {
      "id": 101,
      "fileName": "bai_lam.pdf",
      "filePath": "https://res.cloudinary.com/.../bai_lam.pdf",
      "fileType": "application/pdf",
      "fileSize": 1024000,
      "isDownloadable": true
    }
  ]
}
```

#### **Error Responses:**

**400 - Bad Request:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Score 105.0 exceeds assignment max score 100.0",
  "timestamp": "2025-01-16T10:30:00"
}
```

**403 - Forbidden:**
```json
{
  "error": "ACCESS_DENIED", 
  "message": "Not authorized to grade this submission",
  "timestamp": "2025-01-16T10:30:00"
}
```

**404 - Not Found:**
```json
{
  "error": "RESOURCE_NOT_FOUND",
  "message": "Submission not found with id: 123",
  "timestamp": "2025-01-16T10:30:00"
}
```

### **1.2. Business Logic Chấm Điểm**

#### **Validation Flow:**
```java
// 1. Check submission exists
Submission submission = submissionRepository.findById(submissionId)
    .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

// 2. Check teacher permission
// Teacher chỉ có thể chấm submission của assignment mình tạo
String assignmentTeacher = submission.getAssignment().getCourse().getTeacher().getUserCode();
if (!assignmentTeacher.equals(teacherUsername)) {
    throw new AccessDeniedException("Not authorized to grade this submission");
}

// 3. Validate score within assignment max score
if (score != null && score > assignment.getMaxScore()) {
    throw new BadRequestExceptionCustom("Score exceeds assignment max score");
}

// 4. Update submission
submission.setScore(score);
submission.setFeedback(feedback);
submission.setStatus(SubmissionStatus.GRADED);
submission.setGradedAt(LocalDateTime.now());
submission.setGradedBy(teacher);
```

#### **Score Validation Rules:**
- Score phải >= 0
- Score không được vượt quá `assignment.maxScore`
- Score có thể là null (chỉ cập nhật feedback)
- Có thể re-grade (chấm lại) bao nhiêu lần cũng được
- Score decimal precision: 2 chữ số sau dấu phẩy

#### **Status Transition:**
```
SUBMITTED → GRADED (khi chấm điểm)
LATE → GRADED (khi chấm điểm submission nộp muộn)
GRADED → GRADED (khi chấm lại)
```

---

## 📈 **2. API THỐNG KÊ SUBMISSION**

### **2.1. GET /api/submissions/assignment/{assignmentId}/statistics**

**Mô tả:** Lấy thống kê submission cho một assignment (dành cho giáo viên)

**Method:** `GET`
**URL:** `/api/submissions/assignment/{assignmentId}/statistics`
**Authentication:** Required (Bearer Token)
**Role:** Teacher only

#### **Response Success (200):**
```json
{
  "totalSubmissions": 25,
  "gradedSubmissions": 20,
  "ungradedSubmissions": 5,
  "lateSubmissions": 3,
  
  "averageScore": 78.5,
  "highestScore": 95.0,
  "lowestScore": 45.0,
  "assignmentMaxScore": 100.0,
  
  "submissionRate": 83.33,
  "gradingRate": 80.0,
  "lateRate": 12.0,
  
  "totalStudentsInCourse": 30,
  "studentsNotSubmitted": 5,
  
  "excellentGrades": 8,
  "goodGrades": 7,
  "averageGrades": 3,
  "belowAverageGrades": 2
}
```

#### **Calculation Logic:**

```java
// Basic counts
Long totalSubmissions = submissions.size();
Long gradedSubmissions = submissions.stream()
    .filter(s -> s.getStatus() == SubmissionStatus.GRADED)
    .count();
Long ungradedSubmissions = totalSubmissions - gradedSubmissions;
Long lateSubmissions = submissions.stream()
    .filter(s -> Boolean.TRUE.equals(s.getIsLate()))
    .count();

// Score statistics (chỉ tính submission đã có điểm)
List<Float> scores = submissions.stream()
    .filter(s -> s.getScore() != null)
    .map(Submission::getScore)
    .collect(toList());

Float averageScore = scores.isEmpty() ? null : 
    scores.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
Float highestScore = scores.stream().max(Float::compareTo).orElse(null);
Float lowestScore = scores.stream().min(Float::compareTo).orElse(null);

// Percentages
Double submissionRate = totalStudents > 0 ? 
    (totalSubmissions * 100.0) / totalStudents : 0.0;
Double gradingRate = totalSubmissions > 0 ? 
    (gradedSubmissions * 100.0) / totalSubmissions : 0.0;
Double lateRate = totalSubmissions > 0 ? 
    (lateSubmissions * 100.0) / totalSubmissions : 0.0;

// Grade distribution (dựa trên % của max score)
for (Float score : scores) {
    double percentage = (score / maxScore) * 100;
    if (percentage >= 90) excellentGrades++;
    else if (percentage >= 80) goodGrades++;
    else if (percentage >= 70) averageGrades++;
    else belowAverageGrades++;
}
```

### **2.2. Grade Distribution Categories**

| Category | Percentage Range | Description |
|----------|------------------|-------------|
| **Excellent** | ≥ 90% of max score | Xuất sắc |
| **Good** | 80% - 89% of max score | Khá |  
| **Average** | 70% - 79% of max score | Trung bình |
| **Below Average** | < 70% of max score | Yếu |

### **2.3. Use Cases cho Statistics API**

#### **Teacher Dashboard:**
- Xem tổng quan progress của class
- Identify students chưa nộp bài
- Track grading progress
- Analyze score distribution

#### **Course Management:**
- Compare performance across assignments
- Identify difficult assignments (low average score)
- Monitor submission patterns
- Generate reports for administration

---

## 🔍 **3. API KIỂM TRA SUBMISSION STATUS**

### **3.1. GET /api/submissions/check/{assignmentId}**

**Mô tả:** Kiểm tra student đã nộp assignment chưa

**Method:** `GET`
**URL:** `/api/submissions/check/{assignmentId}`
**Authentication:** Required (Bearer Token)
**Role:** Student only

#### **Response Success (200):**
```json
{
  "hasSubmitted": true
}
```

#### **Use Cases:**
- Student check xem đã nộp bài chưa
- Frontend UI control (show submit button hoặc edit button)
- Validation trước khi allow submission

---

## ⚠️ **4. ERROR HANDLING**

### **4.1. Common Error Scenarios**

| Error Code | Scenario | HTTP Status | Message |
|------------|----------|-------------|---------|
| `VALIDATION_ERROR` | Score vượt quá max score | 400 | "Score X exceeds assignment max score Y" |
| `VALIDATION_ERROR` | Score âm | 400 | "Score must be >= 0" |
| `ACCESS_DENIED` | Student cố gắng chấm điểm | 403 | "Not authorized to grade this submission" |
| `ACCESS_DENIED` | Teacher chấm assignment không phải của mình | 403 | "Not authorized to grade this submission" |
| `RESOURCE_NOT_FOUND` | Submission không tồn tại | 404 | "Submission not found with id: X" |
| `RESOURCE_NOT_FOUND` | Assignment không tồn tại | 404 | "Assignment not found with id: X" |

### **4.2. Validation Details**

#### **Score Validation:**
```java
@DecimalMin(value = "0.0", message = "Score must be >= 0")
@DecimalMax(value = "999.99", message = "Score must be <= 999.99")
private Float score;

// Business validation
if (score != null && assignment.getMaxScore() != null && 
    score > assignment.getMaxScore()) {
    throw new BadRequestExceptionCustom(
        String.format("Score %.2f exceeds assignment max score %.2f", 
                     score, assignment.getMaxScore()));
}
```

#### **Permission Validation:**
```java
// Teacher chỉ được chấm assignment của course mình dạy
String assignmentTeacher = submission.getAssignment().getCourse().getTeacher().getUserCode();
if (!assignmentTeacher.equals(currentUsername)) {
    throw new AccessDeniedException("Not authorized to grade this submission");
}
```

---

## 🧪 **5. TESTING SCENARIOS**

### **5.1. Test Cases cho Grade API**

#### **Valid Test Cases:**
1. **Grade với score hợp lệ**
   - Input: score = 85.5, feedback = "Good work"
   - Expected: 200, submission status = GRADED

2. **Grade chỉ với feedback (no score)**
   - Input: score = null, feedback = "Please resubmit"
   - Expected: 200, score remains null

3. **Re-grade submission**
   - Input: score = 92.0 (update from previous 85.5)
   - Expected: 200, score updated

4. **Grade với max score**
   - Input: score = assignment.maxScore
   - Expected: 200, perfect score

#### **Invalid Test Cases:**
1. **Score vượt quá max score**
   - Input: score = 150 (max = 100)
   - Expected: 400, validation error

2. **Negative score**
   - Input: score = -5
   - Expected: 400, validation error

3. **Student cố gắng grade**
   - Expected: 403, access denied

4. **Teacher grade assignment của teacher khác**
   - Expected: 403, access denied

5. **Grade submission không tồn tại**
   - Expected: 404, not found

### **5.2. Test Cases cho Statistics API**

#### **Valid Test Cases:**
1. **Assignment có submissions**
   - Expected: 200, đầy đủ statistics

2. **Assignment không có submissions**
   - Expected: 200, all counts = 0

3. **Assignment có mix của graded/ungraded**
   - Expected: 200, correct percentage calculations

#### **Invalid Test Cases:**
1. **Assignment không tồn tại**
   - Expected: 404, not found

2. **Student cố gắng access statistics**
   - Expected: 403, access denied

---

## 📝 **6. SAMPLE CURL COMMANDS**

### **6.1. Grade Submission**
```bash
curl -X PUT "http://localhost:8080/api/submissions/123/grade" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "submissionId": 123,
    "score": 85.5,
    "feedback": "Good work, well structured code."
  }'
```

### **6.2. Get Statistics**
```bash
curl -X GET "http://localhost:8080/api/submissions/assignment/1/statistics" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### **6.3. Check Submission Status**
```bash
curl -X GET "http://localhost:8080/api/submissions/check/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📊 **7. PERFORMANCE CONSIDERATIONS**

### **7.1. Statistics Calculation**
- Query optimization cho large datasets
- Có thể cache statistics results
- Consider pagination cho assignments với nhiều submissions

### **7.2. Database Queries**
```sql
-- Count students in course (optimized)
SELECT COUNT(DISTINCT u.id) 
FROM users u 
JOIN enrollments e ON u.id = e.student_id 
WHERE e.course_id = ? AND u.role = 'STUDENT';

-- Get submission statistics (single query)
SELECT 
  COUNT(*) as total_submissions,
  COUNT(CASE WHEN status = 'GRADED' THEN 1 END) as graded_count,
  COUNT(CASE WHEN is_late = true THEN 1 END) as late_count,
  AVG(score) as avg_score,
  MAX(score) as max_score,
  MIN(score) as min_score
FROM submissions 
WHERE assignment_id = ?;
```

---

## 🚀 **8. FUTURE ENHANCEMENTS**

### **8.1. Advanced Grading Features**
- **Rubric-based grading:** Multiple criteria với weights
- **Batch grading:** Grade multiple submissions cùng lúc
- **Grade templates:** Predefined feedback templates
- **Grade history:** Track grading changes over time

### **8.2. Enhanced Statistics**
- **Time-based analytics:** Submission patterns theo ngày/giờ
- **Comparative analytics:** So sánh với assignments khác
- **Student progress tracking:** Individual student analytics
- **Export capabilities:** PDF/Excel reports

### **8.3. Notification System**
- **Auto notifications:** Khi được chấm điểm
- **Deadline reminders:** Trước khi hết hạn nộp
- **Grade distribution alerts:** Khi average score thấp

---

## ✅ **9. IMPLEMENTATION STATUS**

### **Completed Features:**
- ✅ Grade submission API với full validation
- ✅ Statistics API với comprehensive calculations
- ✅ Permission-based access control
- ✅ Error handling và validation
- ✅ Test scripts (PowerShell)
- ✅ Documentation và examples

### **Database Schema:**
- ✅ `score` field trong Submission entity
- ✅ `gradedAt` và `gradedBy` tracking
- ✅ Proper relationships và indexes

### **API Endpoints:**
- ✅ `PUT /api/submissions/{id}/grade`
- ✅ `GET /api/submissions/assignment/{id}/statistics`
- ✅ `GET /api/submissions/check/{assignmentId}`

**Hệ thống chấm điểm và thống kê đã hoàn thiện và sẵn sàng sử dụng!** 🎓
