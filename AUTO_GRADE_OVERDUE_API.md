# 🎯 Auto-Grade Overdue Submissions API

## 📋 **TỔNG QUAN**

API này cho phép giáo viên tự động chấm điểm 0 cho những sinh viên không nộp bài trong assignments đã hết hạn và không cho phép nộp muộn.

---

## 🔧 **API SPECIFICATION**

### **Endpoint:** `POST /api/submissions/assignment/{assignmentId}/auto-grade-overdue`

**Method:** `POST`
**Authentication:** Required (Teacher JWT Token)
**Role:** Teacher only (must be teacher of the course)

---

## 📝 **PARAMETERS**

### **Path Parameter:**
- `assignmentId` (Long): ID của assignment cần auto-grade

### **Headers:**
```http
Authorization: Bearer <teacher_jwt_token>
Content-Type: application/json
```

---

## ✅ **SUCCESS RESPONSE**

### **HTTP Status:** `200 OK`

### **Response Body:**
```json
{
  "message": "Auto-grading completed successfully",
  "processedCount": 3,
  "assignmentId": 1
}
```

### **Response Fields:**
- `message`: Thông báo kết quả
- `processedCount`: Số lượng sinh viên được auto-grade với điểm 0
- `assignmentId`: ID của assignment được xử lý

---

## ❌ **ERROR RESPONSES**

### **1. Assignment Not Found (404)**
```json
{
  "error": "Assignment not found",
  "message": "Assignment not found with id: 999"
}
```

### **2. Assignment Not Overdue (400)**
```json
{
  "error": "Bad Request",
  "message": "Assignment is not yet overdue. Due date: 2025-07-01T23:59:59"
}
```

### **3. Late Submission Allowed (400)**
```json
{
  "error": "Bad Request", 
  "message": "Assignment allows late submission. Cannot auto-grade with zero."
}
```

### **4. Permission Denied (400)**
```json
{
  "error": "Bad Request",
  "message": "You don't have permission to grade this assignment"
}
```

### **5. Teacher Not Found (404)**
```json
{
  "error": "Teacher not found",
  "message": "Teacher not found: teacher01"
}
```

---

## 🎯 **BUSINESS LOGIC**

### **Điều Kiện Thực Hiện:**
1. ✅ Assignment phải **đã hết hạn** (past due date)
2. ✅ Assignment **KHÔNG cho phép** nộp muộn (`allowLateSubmission = false`)
3. ✅ User phải là **teacher** của course chứa assignment
4. ✅ Assignment phải **tồn tại** và **thuộc về course**

### **Quy Trình Auto-Grade:**
1. **Validate Assignment:** Kiểm tra assignment tồn tại và đã hết hạn
2. **Check Permissions:** Xác nhận teacher có quyền grade assignment này
3. **Get Enrolled Students:** Lấy tất cả sinh viên đã enroll course
4. **Find Non-Submitters:** Tìm sinh viên chưa có submission
5. **Create Zero Submissions:** Tạo submission với score = 0 cho mỗi sinh viên
6. **Auto-Feedback:** Gán feedback tự động

### **Submission được tạo có:**
```java
{
  "score": 0.0,
  "feedback": "Automatic grade: Assignment not submitted by due date",
  "status": "SUBMITTED",
  "submittedAt": "<assignment_due_date>",
  "gradedAt": "<current_timestamp>",
  "isLate": false
}
```

---

## 📚 **USAGE EXAMPLES**

### **1. Successful Auto-Grade**
```bash
curl -X POST "http://localhost:8080/api/submissions/assignment/1/auto-grade-overdue" \
  -H "Authorization: Bearer <teacher_jwt_token>" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "message": "Auto-grading completed successfully",
  "processedCount": 5,
  "assignmentId": 1
}
```

### **2. Assignment Not Overdue**
```bash
# Assignment due date: 2025-07-01T23:59:59
# Current time: 2025-06-28T10:00:00
curl -X POST "http://localhost:8080/api/submissions/assignment/2/auto-grade-overdue" \
  -H "Authorization: Bearer <teacher_jwt_token>"
```

**Response (400):**
```json
{
  "error": "Assignment is not yet overdue. Due date: 2025-07-01T23:59:59"
}
```

### **3. Late Submission Allowed**
```bash
# Assignment.allowLateSubmission = true
curl -X POST "http://localhost:8080/api/submissions/assignment/3/auto-grade-overdue" \
  -H "Authorization: Bearer <teacher_jwt_token>"
```

**Response (400):**
```json
{
  "error": "Assignment allows late submission. Cannot auto-grade with zero."
}
```

---

## 🔄 **WORKFLOW EXAMPLE**

### **Scenario:** Teacher muốn finalize grading cho assignment đã hết hạn

1. **Check Assignment Status:**
   ```bash
   GET /api/assignments/detail/1
   # Response: dueDate="2025-06-25T23:59:59", allowLateSubmission=false
   ```

2. **Check Current Statistics:**
   ```bash
   GET /api/submissions/assignment/1/statistics
   # Response: totalStudents=30, totalSubmissions=25, studentsNotSubmitted=5
   ```

3. **Perform Auto-Grade:**
   ```bash
   POST /api/submissions/assignment/1/auto-grade-overdue
   # Response: processedCount=5
   ```

4. **Verify Results:**
   ```bash
   GET /api/submissions/assignment/1/statistics
   # Response: totalStudents=30, totalSubmissions=30, studentsNotSubmitted=0
   ```

5. **Review Zero Submissions:**
   ```bash
   GET /api/submissions/assignment/1?current=1&pageSize=50
   # Filter results with score=0 and automatic feedback
   ```

---

## ⚠️ **IMPORTANT NOTES**

### **Safety Measures:**
- ✅ **Idempotent:** Có thể gọi nhiều lần, chỉ tạo submission cho student chưa có
- ✅ **Permission Check:** Chỉ teacher của course mới có quyền
- ✅ **Validation:** Kiểm tra đầy đủ điều kiện trước khi thực hiện
- ✅ **Audit Trail:** Có timestamp và feedback rõ ràng

### **Data Integrity:**
- ✅ Không overwrites submissions đã có
- ✅ Sử dụng assignment due date làm submitted timestamp
- ✅ Mark isLate = false (vì là auto-grade)
- ✅ Automatic feedback để phân biệt với manual submissions

### **Best Practices:**
1. **Timing:** Chỉ thực hiện sau khi assignment đã hết hạn một thời gian
2. **Communication:** Thông báo cho sinh viên trước khi auto-grade
3. **Review:** Kiểm tra kết quả sau khi auto-grade
4. **Grace Period:** Cân nhắc cho phép grace period trước khi auto-grade

---

## 🧪 **TESTING**

### **Test Script:**
```powershell
.\test_auto_grade_overdue.ps1
```

### **Test Scenarios:**
1. ✅ Successful auto-grade with multiple students
2. ✅ Assignment not overdue
3. ✅ Late submission allowed
4. ✅ Permission denied (wrong teacher)
5. ✅ Assignment not found
6. ✅ Idempotent behavior (run twice)

---

## 📊 **MONITORING**

### **Logs to Monitor:**
```java
log.info("🔄 Starting auto-grade process for assignment ID: {} by user: {}", assignmentId, username);
log.info("👤 Creating zero submission for student: {} ({})", student.getFullName(), student.getUserCode());
log.info("🎯 Auto-grade completed: {} students processed with zero score", processedCount);
```

### **Metrics to Track:**
- Number of auto-graded submissions per assignment
- Time taken for auto-grade process
- Error rates and reasons
- Teacher usage patterns

**API này giúp teachers efficiently finalize grading cho overdue assignments! 🎯**
