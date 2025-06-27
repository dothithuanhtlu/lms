# 📚 TÀI LIỆU HỆ THỐNG SUBMISSION - LMS

## 📋 **1. TỔNG QUAN HỆ THỐNG**

### **Mục đích**
Hệ thống submission xử lý việc nộp bài tập của học sinh trong Learning Management System (LMS), bao gồm:
- Quản lý việc nộp bài của học sinh
- Theo dõi trạng thái submission 
- Xử lý deadline và nộp muộn
- Chấm điểm và feedback
- Upload/download files

### **Kiến trúc tổng quan**
```
Student ←→ SubmissionController ←→ SubmissionService ←→ Database
                ↓                        ↓
        CourseController ←→ CourseService ←→ Cloudinary Storage
```

---

## 🏗️ **2. CẤU TRÚC DATABASE**

### **2.1. Submission Entity**
```java
@Entity
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "student_id")
    private User student;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;        // Thời điểm nộp bài
    
    @Column(name = "score")
    private Float score;                      // Điểm số (null = chưa chấm)
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;                  // Nhận xét từ giáo viên
    
    @Column(name = "graded_at")
    private LocalDateTime gradedAt;           // Thời điểm chấm điểm
    
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;          // Trạng thái submission
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL)
    private List<SubmissionDocument> documents; // Files đính kèm
    
    // Note: gradedBy field removed for optimization
    // Grader info can be derived from assignment.course.teacher
}
```

### **2.2. SubmissionStatus Enum**
```java
public enum SubmissionStatus {
    SUBMITTED("Đã nộp"),
    GRADED("Đã chấm điểm"), 
    RETURNED("Đã trả bài"),
    LATE("Nộp muộn");
    
    private final String description;
}
```

### **2.3. SubmissionDocument Entity**
```java
@Entity
@Table(name = "submission_documents")
public class SubmissionDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;
    
    @Column(name = "file_name_original")
    private String fileNameOriginal;
    
    @Column(name = "file_path")
    private String filePath;                  // URL trên Cloudinary
    
    @Column(name = "file_type")
    private String fileType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}
```

---

## 🔄 **3. LOGIC XỬ LÝ SUBMISSION**

### **3.1. Logic Phân Loại Trạng Thái (CourseService.java:188-220)**

```java
// Trong CourseService.getCourseFullDetails()
dto.getAssignments().forEach(assignment -> {
    Long assignmentId = assignment.getId();
    Long studentId = student.getId();
    
    // Kiểm tra student đã nộp bài này chưa
    Optional<Submission> submission = 
        submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
    
    String status;
    LocalDateTime submissionDate = null;
    Float score = null;
    boolean hasSubmitted = submission.isPresent();
    
    if (hasSubmitted) {
        Submission sub = submission.get();
        submissionDate = sub.getSubmittedAt();
        score = sub.getScore();
        
        // Xác định trạng thái dựa vào điều kiện
        if (sub.getScore() != null) {
            status = "GRADED";        // Đã chấm điểm
        } else if (submissionDate != null && assignment.getDueDate() != null && 
                   submissionDate.isAfter(assignment.getDueDate())) {
            status = "LATE";          // Nộp muộn, chưa chấm
        } else {
            status = "SUBMITTED";     // Nộp đúng hạn, chưa chấm
        }
    } else {
        status = "NOT_SUBMITTED";     // Chưa nộp
    }
});
```

### **3.2. Các Trạng Thái Submission**

| Trạng thái | Điều kiện | Ý nghĩa | Action khả dụng |
|------------|-----------|---------|-----------------|
| `NOT_SUBMITTED` | `submission.isEmpty()` | Chưa nộp bài | Create, Submit |
| `SUBMITTED` | `hasSubmitted && score == null && !isLate` | Nộp đúng hạn, chờ chấm | Update, Delete |
| `LATE` | `hasSubmitted && score == null && isLate` | Nộp muộn, chờ chấm | View only |
| `GRADED` | `hasSubmitted && score != null` | Đã chấm điểm xong | View only |

### **3.3. Logic Kiểm Tra Nộp Muộn**

```java
// So sánh thời điểm nộp với deadline
boolean isLate = submissionDate.isAfter(assignment.getDueDate());

// Kiểm tra assignment có cho phép nộp muộn không
Boolean allowLateSubmission = assignment.getAllowLateSubmission();

// Validation khi nộp bài
if (isLate && !allowLateSubmission) {
    throw new BadRequestExceptionCustom("Late submissions are not allowed for this assignment");
}
```

---

## 🛠️ **4. API ENDPOINTS**

### **4.1. Student APIs - Quản Lý Submission**

#### **4.1.1. POST /api/submissions/submit - Nộp bài tập**
```http
POST /api/submissions/submit
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}

Form Data:
- assignmentId: 1
- content: "Nội dung bài làm" (optional)
- files: [file1.pdf, file2.docx] (optional, max 10 files, 100MB each)
```

**Response Success (201):**
```json
{
  "id": 123,
  "assignmentId": 1,
  "assignmentTitle": "Bài tập lập trình Java",
  "studentName": "Nguyễn Văn A",
  "studentCode": "SV001",
  "submittedAt": "2025-01-15T14:30:00",
  "status": "SUBMITTED",
  "isLate": false,
  "score": null,
  "feedback": null,
  "documents": [
    {
      "id": 456,
      "fileName": "bai_lam.pdf",
      "filePath": "https://res.cloudinary.com/.../bai_lam.pdf",
      "fileSize": 1024000,
      "fileType": "application/pdf"
    }
  ]
}
```

**Response Error (400):**
```json
{
  "error": "LATE_SUBMISSION_NOT_ALLOWED",
  "message": "Late submissions are not allowed for this assignment",
  "timestamp": "2025-01-15T14:30:00"
}
```

#### **4.1.2. PUT /api/submissions/{id}/update - Cập nhật bài nộp**
```http
PUT /api/submissions/{submissionId}/update
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}

Form Data:
- content: "Nội dung cập nhật"
- files: [new_file.pdf]
- deleteFileIds: [123, 456] (IDs của files cần xóa)
```

#### **4.1.3. GET /api/submissions/my-submissions - Xem bài đã nộp**
```http
GET /api/submissions/my-submissions?page=0&size=10&courseId=1
Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 123,
      "assignmentTitle": "Bài tập 1",
      "courseName": "Lập trình Java",
      "submittedAt": "2025-01-15T14:30:00",
      "status": "GRADED",
      "score": 85.5,
      "isLate": false,
      "dueDate": "2025-01-14T23:59:59"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 15,
  "totalPages": 2
}
```

#### **4.1.4. GET /api/submissions/check/{assignmentId} - Kiểm tra đã nộp chưa**
```http
GET /api/submissions/check/{assignmentId}
Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "assignmentId": 1,
  "hasSubmitted": true,
  "submissionId": 123,
  "submittedAt": "2025-01-15T14:30:00",
  "status": "GRADED",
  "score": 85.5,
  "isLate": false,
  "allowEdit": false,
  "allowDelete": false
}
```

#### **4.1.5. DELETE /api/submissions/{id} - Xóa bài nộp**
```http
DELETE /api/submissions/{submissionId}
Authorization: Bearer {jwt_token}
```

**Conditions:**
- Chỉ xóa được khi status = "SUBMITTED" hoặc "LATE"
- Không thể xóa sau khi được chấm điểm

### **4.2. Teacher APIs - Chấm Điểm và Quản Lý**

#### **4.2.1. PUT /api/submissions/{id}/grade - Chấm điểm**
```http
PUT /api/submissions/{submissionId}/grade
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "score": 85.5,
  "feedback": "Bài làm tốt, cần cải thiện phần kết luận. Logic code rõ ràng nhưng thiếu comment."
}
```

**Response:**
```json
{
  "id": 123,
  "score": 85.5,
  "feedback": "Bài làm tốt, cần cải thiện phần kết luận...",
  "gradedAt": "2025-01-16T10:30:00",
  "gradedBy": "Nguyễn Thị B",
  "status": "GRADED"
}
```

#### **4.2.2. GET /api/submissions/assignment/{assignmentId} - Xem tất cả bài nộp**
```http
GET /api/submissions/assignment/{assignmentId}?page=0&size=10&status=SUBMITTED
Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 123,
      "studentName": "Nguyễn Văn A",
      "studentCode": "SV001",
      "submittedAt": "2025-01-15T14:30:00",
      "status": "SUBMITTED",
      "score": null,
      "isLate": false,
      "filesCount": 2
    },
    {
      "id": 124,
      "studentName": "Trần Thị C",
      "studentCode": "SV002", 
      "submittedAt": "2025-01-16T02:15:00",
      "status": "LATE",
      "score": null,
      "isLate": true,
      "filesCount": 1
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "statistics": {
    "totalSubmissions": 25,
    "gradedCount": 10,
    "pendingCount": 12,
    "lateCount": 3,
    "averageScore": 78.5
  }
}
```

### **4.3. Course Full Details API - Theo Dõi Tổng Quan**

#### **4.3.1. GET /admin/courses/{courseId}/full-details**
```http
GET /admin/courses/{courseId}/full-details
Authorization: Bearer {jwt_token}
```

**Response với Submission Status:**
```json
{
  "id": 1,
  "courseCode": "CS101",
  "courseName": "Lập trình Java cơ bản",
  "description": "Khóa học lập trình Java cho người mới bắt đầu",
  "startDate": "2025-01-10",
  "endDate": "2025-05-10",
  "maxStudents": 30,
  "currentStudents": 25,
  "status": "Active",
  "assignments": [
    {
      "id": 1,
      "title": "Bài tập 1: Hello World",
      "description": "Viết chương trình Hello World đầu tiên",
      "maxScore": 100.0,
      "dueDate": "2025-01-20T23:59:59",
      "isPublished": true,
      "totalSubmissions": 20
    },
    {
      "id": 2,
      "title": "Bài tập 2: Biến và Kiểu dữ liệu",
      "description": "Làm quen với biến và các kiểu dữ liệu",
      "maxScore": 100.0,
      "dueDate": "2025-01-27T23:59:59",
      "isPublished": true,
      "totalSubmissions": 15
    }
  ],
  "students": [
    {
      "id": 1,
      "userCode": "SV001",
      "fullName": "Nguyễn Văn A",
      "email": "student1@example.com",
      "className": "CNTT-K63",
      "enrollmentStatus": "ACTIVE",
      "midtermScore": 85.5,
      "finalScore": 90.0,
      "assignmentSubmissions": [
        {
          "assignmentId": 1,
          "assignmentTitle": "Bài tập 1: Hello World",
          "hasSubmitted": true,
          "submissionDate": "2025-01-18T14:30:00",
          "score": 95.0,
          "status": "GRADED"
        },
        {
          "assignmentId": 2,
          "assignmentTitle": "Bài tập 2: Biến và Kiểu dữ liệu",
          "hasSubmitted": true,
          "submissionDate": "2025-01-28T01:15:00",
          "score": null,
          "status": "LATE"
        }
      ]
    },
    {
      "id": 2,
      "userCode": "SV002",
      "fullName": "Trần Thị B",
      "email": "student2@example.com",
      "className": "CNTT-K63",
      "enrollmentStatus": "ACTIVE",
      "midtermScore": 78.0,
      "finalScore": null,
      "assignmentSubmissions": [
        {
          "assignmentId": 1,
          "assignmentTitle": "Bài tập 1: Hello World",
          "hasSubmitted": false,
          "submissionDate": null,
          "score": null,
          "status": "NOT_SUBMITTED"
        },
        {
          "assignmentId": 2,
          "assignmentTitle": "Bài tập 2: Biến và Kiểu dữ liệu",
          "hasSubmitted": true,
          "submissionDate": "2025-01-26T16:45:00",
          "score": 82.0,
          "status": "GRADED"
        }
      ]
    }
  ]
}
```

---

## 📊 **5. WORKFLOW NỘP BÀI**

### **5.1. Quy Trình Student Nộp Bài**

```mermaid
graph TD
    A[Student truy cập assignment] --> B{Đã nộp chưa?}
    B -->|Chưa| C[Hiển thị form nộp bài]
    B -->|Rồi| D{Status của submission}
    D -->|SUBMITTED/LATE| E[Cho phép edit/delete]
    D -->|GRADED| F[Chỉ xem, không edit]
    C --> G[Student upload files + content]
    G --> H[Validate files and data]
    H --> I{Valid?}
    I -->|No| J[Show error message]
    I -->|Yes| K[Upload files to Cloudinary]
    K --> L[Save submission to DB]
    L --> M{Kiểm tra deadline}
    M -->|Đúng hạn| N[Status: SUBMITTED]
    M -->|Nộp muộn| O{Cho phép nộp muộn?}
    O -->|Có| P[Status: LATE]
    O -->|Không| Q[Reject submission]
    N --> R[Thành công]
    P --> R
    J --> C
    Q --> C
```

### **5.2. Quy Trình Teacher Chấm Bài**

```mermaid
graph TD
    A[Teacher vào assignment] --> B[Xem danh sách submissions]
    B --> C[Filter theo status/student]
    C --> D[Chọn submission cần chấm]
    D --> E[Download và xem files]
    E --> F[Đánh giá bài làm]
    F --> G[Nhập điểm và feedback]
    G --> H[Submit grade]
    H --> I[Update status: GRADED]
    I --> J[Send notification to student]
    J --> K[Update statistics]
```

### **5.3. Status Transition Diagram**

```mermaid
stateDiagram-v2
    [*] --> NOT_SUBMITTED : Assignment created
    NOT_SUBMITTED --> SUBMITTED : Submit on time
    NOT_SUBMITTED --> LATE : Submit after deadline
    SUBMITTED --> GRADED : Teacher grades
    LATE --> GRADED : Teacher grades  
    SUBMITTED --> NOT_SUBMITTED : Student deletes
    LATE --> NOT_SUBMITTED : Student deletes
    GRADED --> [*] : Final status
```

---

## 🔐 **6. BẢO MẬT VÀ PHÂN QUYỀN**

### **6.1. Student Permissions**
```java
// StudentController Security
@PreAuthorize("hasRole('STUDENT')")
public class StudentSubmissionController {
    
    // Chỉ được thao tác với submission của chính mình
    @PreAuthorize("@submissionService.isOwner(#submissionId, authentication.name)")
    public ResponseEntity<?> updateSubmission(@PathVariable Long submissionId) {
        // ...
    }
}
```

**Quyền của Student:**
- ✅ Tạo submission cho assignment của khóa học đã đăng ký
- ✅ Xem và cập nhật submission của chính mình
- ✅ Xóa submission chưa được chấm điểm
- ✅ Download files của submission mình đã nộp
- ❌ Không thể xem submission của student khác
- ❌ Không thể chấm điểm
- ❌ Không thể xem submission sau khi đã GRADED

### **6.2. Teacher Permissions**
```java
// TeacherController Security  
@PreAuthorize("hasRole('TEACHER')")
public class TeacherSubmissionController {
    
    // Chỉ được chấm bài của assignment mình tạo
    @PreAuthorize("@assignmentService.isTeacherOfAssignment(#assignmentId, authentication.name)")
    public ResponseEntity<?> gradeSubmissions(@PathVariable Long assignmentId) {
        // ...
    }
}
```

**Quyền của Teacher:**
- ✅ Xem tất cả submissions của assignment mình tạo
- ✅ Chấm điểm và feedback cho submissions
- ✅ Download files của submissions để review
- ✅ Xem thống kê submissions (số lượng, điểm trung bình)
- ✅ Export danh sách submissions ra Excel/CSV
- ❌ Không thể sửa nội dung submission của student
- ❌ Không thể xem submissions của teacher khác
- ❌ Không thể xóa submissions

### **6.3. Security Validations**
```java
// SubmissionService.java
public void validateSubmissionAccess(Long submissionId, String userCode, String role) {
    Submission submission = submissionRepository.findById(submissionId)
        .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
    
    if ("STUDENT".equals(role)) {
        // Student chỉ được access submission của mình
        if (!submission.getStudent().getUserCode().equals(userCode)) {
            throw new AccessDeniedException("Not authorized to access this submission");
        }
    } else if ("TEACHER".equals(role)) {
        // Teacher chỉ được access submission của assignment mình tạo
        String assignmentTeacher = submission.getAssignment().getCourse().getTeacher().getUserCode();
        if (!assignmentTeacher.equals(userCode)) {
            throw new AccessDeniedException("Not authorized to access this submission");
        }
    }
}

// Validation khi nộp bài
public void validateSubmissionCreate(Long assignmentId, String studentCode) {
    Assignment assignment = assignmentRepository.findById(assignmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    
    // Kiểm tra student có enrolled vào course không
    boolean isEnrolled = enrollmentRepository.existsByCourseIdAndStudentUserCode(
        assignment.getCourse().getId(), studentCode);
    if (!isEnrolled) {
        throw new BadRequestExceptionCustom("Student not enrolled in this course");
    }
    
    // Kiểm tra đã nộp chưa
    boolean alreadySubmitted = submissionRepository.existsByAssignmentIdAndStudentUserCode(
        assignmentId, studentCode);
    if (alreadySubmitted) {
        throw new BadRequestExceptionCustom("Assignment already submitted");
    }
    
    // Kiểm tra assignment có published không
    if (!assignment.getIsPublished()) {
        throw new BadRequestExceptionCustom("Assignment is not published yet");
    }
    
    // Kiểm tra deadline và late submission
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime dueDate = assignment.getDueDate();
    
    if (dueDate != null && now.isAfter(dueDate)) {
        Boolean allowLateSubmission = assignment.getAllowLateSubmission();
        if (allowLateSubmission == null || !allowLateSubmission) {
            throw new BadRequestExceptionCustom("Late submissions are not allowed for this assignment");
        }
    }
}
```

---

## 💾 **7. FILE MANAGEMENT**

### **7.1. Cloudinary Storage Structure**
```
lms/
├── submissions/
│   ├── {submissionId}/
│   │   ├── document1.pdf
│   │   ├── document2.docx
│   │   ├── source_code.zip
│   │   └── screenshot.jpg
│   └── {submissionId2}/
│       ├── report.pdf
│       └── presentation.pptx
```

### **7.2. Upload Process**
```java
// SubmissionService.uploadFiles()
public List<SubmissionDocument> uploadFiles(Submission submission, MultipartFile[] files) {
    List<SubmissionDocument> documents = new ArrayList<>();
    
    // 1. Validate files
    validateFiles(files);
    
    // 2. Upload to Cloudinary
    String folderName = "lms/submissions/" + submission.getId();
    List<Map> uploadResults = cloudinaryService.uploadMultipleFiles(
        Arrays.asList(files), folderName);
    
    // 3. Save file info to database
    for (int i = 0; i < files.length; i++) {
        MultipartFile file = files[i];
        Map uploadResult = uploadResults.get(i);
        
        if (!uploadResult.containsKey("error")) {
            SubmissionDocument document = new SubmissionDocument();
            document.setSubmission(submission);
            document.setFileNameOriginal(file.getOriginalFilename());
            document.setFilePath((String) uploadResult.get("secure_url"));
            document.setFileType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setUploadedAt(LocalDateTime.now());
            
            documents.add(submissionDocumentRepository.save(document));
        }
    }
    
    return documents;
}

// File validation
private void validateFiles(MultipartFile[] files) {
    if (files == null) return;
    
    if (files.length > 10) {
        throw new BadRequestExceptionCustom("Maximum 10 files allowed per submission");
    }
    
    for (MultipartFile file : files) {
        // Check file size (100MB max)
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new BadRequestExceptionCustom(
                String.format("File '%s' is too large. Maximum 100MB allowed.", 
                    file.getOriginalFilename()));
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (!isAllowedFileType(contentType)) {
            throw new BadRequestExceptionCustom(
                String.format("File type '%s' is not allowed.", contentType));
        }
    }
}

// Allowed file types
private boolean isAllowedFileType(String contentType) {
    List<String> allowedTypes = Arrays.asList(
        "application/pdf",
        "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain",
        "image/jpeg",
        "image/png",
        "image/gif",
        "application/zip",
        "application/x-zip-compressed"
    );
    return allowedTypes.contains(contentType);
}
```

### **7.3. Download Process**
```java
// File download with security check
@GetMapping("/submissions/{submissionId}/documents/{documentId}/download")
@PreAuthorize("@submissionService.canAccessDocument(#submissionId, #documentId, authentication.name)")
public ResponseEntity<?> downloadDocument(
        @PathVariable Long submissionId,
        @PathVariable Long documentId) {
    
    SubmissionDocument document = submissionDocumentRepository.findById(documentId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    
    // Log download activity
    auditService.logFileDownload(submissionId, documentId, getCurrentUserCode());
    
    // Return redirect to Cloudinary URL
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(document.getFilePath()))
        .build();
}
```

---

## 📈 **8. THỐNG KÊ VÀ BÁO CÁO**

### **8.1. Assignment Statistics**
```java
// AssignmentService.getAssignmentStatistics()
public AssignmentStatisticsDTO getAssignmentStatistics(Long assignmentId) {
    Assignment assignment = findAssignmentById(assignmentId);
    
    // Basic counts
    long totalSubmissions = submissionRepository.countByAssignmentId(assignmentId);
    long gradedCount = submissionRepository.countByAssignmentIdAndScoreIsNotNull(assignmentId);
    long pendingCount = totalSubmissions - gradedCount;
    long lateCount = submissionRepository.countByAssignmentIdAndStatus(
        assignmentId, SubmissionStatus.LATE);
    
    // Score statistics
    Double averageScore = submissionRepository.getAverageScoreByAssignmentId(assignmentId);
    Float maxScore = submissionRepository.getMaxScoreByAssignmentId(assignmentId);
    Float minScore = submissionRepository.getMinScoreByAssignmentId(assignmentId);
    
    // Submission timeline
    List<SubmissionTimelineDTO> timeline = getSubmissionTimeline(assignmentId);
    
    return AssignmentStatisticsDTO.builder()
        .assignmentId(assignmentId)
        .assignmentTitle(assignment.getTitle())
        .totalStudents(enrollmentRepository.countByCourseId(assignment.getCourse().getId()))
        .totalSubmissions(totalSubmissions)
        .gradedCount(gradedCount)
        .pendingCount(pendingCount)
        .lateCount(lateCount)
        .submissionRate((double) totalSubmissions / getTotalStudents(assignmentId) * 100)
        .averageScore(averageScore)
        .maxScore(maxScore)
        .minScore(minScore)
        .timeline(timeline)
        .build();
}
```

### **8.2. Course Progress Tracking (CourseService.java:182-220)**
```java
// Tracking submission status for each student
student.setAssignmentSubmissions(submissionStatuses);

// Response format for course full details
{
  "students": [
    {
      "assignmentSubmissions": [
        {
          "assignmentId": 1,
          "assignmentTitle": "Bài tập 1",
          "hasSubmitted": true,
          "submissionDate": "2025-01-15T14:30:00", 
          "score": 85.0,
          "status": "GRADED"
        }
      ]
    }
  ]
}
```

### **8.3. Student Progress Dashboard**
```java
// StudentService.getStudentProgress()
public StudentProgressDTO getStudentProgress(String studentCode, Long courseId) {
    List<Assignment> assignments = assignmentRepository.findByCourseIdAndIsPublished(courseId, true);
    List<Submission> submissions = submissionRepository.findByStudentUserCodeAndCourseId(
        studentCode, courseId);
    
    Map<Long, Submission> submissionMap = submissions.stream()
        .collect(Collectors.toMap(s -> s.getAssignment().getId(), s -> s));
    
    List<StudentAssignmentProgressDTO> progress = assignments.stream()
        .map(assignment -> {
            Submission submission = submissionMap.get(assignment.getId());
            return StudentAssignmentProgressDTO.builder()
                .assignmentId(assignment.getId())
                .assignmentTitle(assignment.getTitle())
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .hasSubmitted(submission != null)
                .submissionDate(submission != null ? submission.getSubmittedAt() : null)
                .score(submission != null ? submission.getScore() : null)
                .status(getSubmissionStatus(submission, assignment))
                .isLate(isLateSubmission(submission, assignment))
                .build();
        })
        .collect(Collectors.toList());
    
    return StudentProgressDTO.builder()
        .studentCode(studentCode)
        .courseId(courseId)
        .totalAssignments(assignments.size())
        .submittedCount((int) progress.stream().filter(p -> p.getHasSubmitted()).count())
        .gradedCount((int) progress.stream().filter(p -> p.getScore() != null).count())
        .averageScore(calculateAverageScore(progress))
        .assignments(progress)
        .build();
}
```

---

## ⚠️ **9. XỬ LÝ LỖI VÀ VALIDATION**

### **9.1. Business Rules**
```java
public class SubmissionValidationRules {
    
    // File constraints
    public static final int MAX_FILES_PER_SUBMISSION = 10;
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    
    // Submission constraints  
    public static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
        "application/pdf", "application/msword", "text/plain",
        "image/jpeg", "image/png", "application/zip"
    );
    
    // Business rules
    public static void validateSubmissionCreate(Assignment assignment, String studentCode) {
        // Rule 1: Assignment must be published
        if (!assignment.getIsPublished()) {
            throw new BadRequestExceptionCustom("Assignment is not published yet");
        }
        
        // Rule 2: Student must be enrolled in course
        // ... validation logic
        
        // Rule 3: Cannot submit after deadline (unless allowed)
        if (isAfterDeadline(assignment) && !isLateSubmissionAllowed(assignment)) {
            throw new BadRequestExceptionCustom("Late submissions are not allowed");
        }
        
        // Rule 4: Cannot submit twice
        if (hasAlreadySubmitted(assignment.getId(), studentCode)) {
            throw new BadRequestExceptionCustom("Assignment already submitted");
        }
    }
    
    public static void validateSubmissionUpdate(Submission submission) {
        // Rule 1: Cannot update after graded
        if (submission.getScore() != null) {
            throw new BadRequestExceptionCustom("Cannot update graded submission");
        }
        
        // Rule 2: Cannot update after assignment deadline (in some cases)
        // ... additional rules
    }
    
    public static void validateGrading(Submission submission, Float score, String teacherCode) {
        // Rule 1: Only assignment teacher can grade
        String assignmentTeacher = submission.getAssignment().getCourse().getTeacher().getUserCode();
        if (!assignmentTeacher.equals(teacherCode)) {
            throw new AccessDeniedException("Not authorized to grade this submission");
        }
        
        // Rule 2: Score must be within valid range
        Float maxScore = submission.getAssignment().getMaxScore();
        if (score < 0 || score > maxScore) {
            throw new BadRequestExceptionCustom(
                String.format("Score must be between 0 and %.1f", maxScore));
        }
    }
}
```

### **9.2. Error Response Format**
```java
// Global Exception Handler
@ControllerAdvice
public class SubmissionExceptionHandler {
    
    @ExceptionHandler(BadRequestExceptionCustom.class)
    public ResponseEntity<?> handleBadRequest(BadRequestExceptionCustom ex) {
        ErrorResponse error = ErrorResponse.builder()
            .error("VALIDATION_ERROR")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .error("ACCESS_DENIED")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .error("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

### **9.3. Common Error Scenarios**
| Error Code | Scenario | HTTP Status | Solution |
|------------|----------|-------------|----------|
| `LATE_SUBMISSION_NOT_ALLOWED` | Nộp sau deadline mà không cho phép | 400 | Check allowLateSubmission |
| `ASSIGNMENT_ALREADY_SUBMITTED` | Nộp lại assignment đã nộp | 400 | Use update endpoint |
| `FILE_TOO_LARGE` | File quá 100MB | 400 | Compress or split file |
| `TOO_MANY_FILES` | Quá 10 files | 400 | Reduce number of files |
| `INVALID_FILE_TYPE` | Loại file không được phép | 400 | Use allowed file types |
| `ASSIGNMENT_NOT_PUBLISHED` | Assignment chưa publish | 400 | Wait for teacher to publish |
| `NOT_ENROLLED` | Student chưa enroll course | 403 | Enroll in course first |
| `SUBMISSION_NOT_FOUND` | Submission không tồn tại | 404 | Check submission ID |
| `CANNOT_UPDATE_GRADED` | Cập nhật submission đã chấm | 400 | Contact teacher |

---

## 🎯 **10. KẾT LUẬN VÀ ROADMAP**

### **10.1. Tính năng hiện có ✅**
- **Complete CRUD operations** cho submissions
- **File upload/download** với Cloudinary integration  
- **Late submission detection** và validation
- **Grading workflow** với score và feedback
- **Security & permissions** theo role (Student/Teacher)
- **Progress tracking** và statistics
- **Error handling** và validation rules
- **Multi-file support** với size/type constraints

### **10.2. CourseService Logic ✅**
```java
// Lines 182-220: Assignment submission status tracking
// ✅ Kiểm tra hasSubmitted
// ✅ So sánh submissionDate với dueDate để detect late
// ✅ Phân loại status: NOT_SUBMITTED, SUBMITTED, LATE, GRADED
// ✅ Trả về đầy đủ thông tin trong API response
```

### **10.3. Roadmap - Tính năng có thể mở rộng 🚀**

#### **Phase 1: Enhanced Late Submission**
- [ ] **Penalty system** cho bài nộp muộn (trừ điểm theo ngày)
- [ ] **Grace period** (thời gian gia hạn) cho assignments
- [ ] **Automatic status updates** (NOT_SUBMITTED → OVERDUE)
- [ ] **Email notifications** cho deadline warnings

#### **Phase 2: Advanced Features**
- [ ] **Plagiarism detection** integration
- [ ] **Peer review** system
- [ ] **Rubric-based grading** 
- [ ] **Group assignments** support
- [ ] **Version control** cho submissions (multiple drafts)

#### **Phase 3: Analytics & Reporting**
- [ ] **Advanced analytics** dashboard
- [ ] **Submission patterns** analysis
- [ ] **Export reports** (Excel, PDF)
- [ ] **Performance predictions** based on submission behavior

#### **Phase 4: Mobile & Integration**
- [ ] **Mobile app** support
- [ ] **LTI integration** cho external tools
- [ ] **Google Classroom** sync
- [ ] **GitHub integration** cho code submissions

### **10.4. System Ready Status 🎓**

**Hệ thống submission hiện tại đã sẵn sàng cho production với:**
- ✅ **Đầy đủ API endpoints** cho Student và Teacher workflows
- ✅ **Robust security** và permission handling  
- ✅ **Complete file management** với Cloudinary
- ✅ **Comprehensive error handling** và validation
- ✅ **Real-time progress tracking** trong Course Full Details API
- ✅ **Scalable architecture** có thể mở rộng thêm tính năng

**System đáp ứng đầy đủ requirements cho một LMS submission module hoàn chỉnh!** 🚀

---

## 📞 **11. SUPPORT & MAINTENANCE**

### **11.1. Monitoring Points**
- File upload success rate
- Submission API response times  
- Cloudinary storage usage
- Database query performance
- Error rates by endpoint

### **11.2. Regular Maintenance**
- Clean up orphaned files in Cloudinary
- Archive old submissions  
- Update file type restrictions
- Monitor storage quotas
- Performance optimization

### **11.3. Documentation Updates**
- API documentation (Swagger/OpenAPI)
- Database schema documentation
- Security guidelines
- Deployment procedures
- Troubleshooting guides
