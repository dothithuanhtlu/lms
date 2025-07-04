# Sequence Diagram: View Course Use Case

## Overview
Sequence diagram cho use case "Xem khóa học" (View Course) với các actor: Admin, Teacher, Student.

## Sequence Diagram - View Course Use Case

### Complete Sequence Diagram
```plantuml
@startuml "View Course Sequence Diagram"
!theme plain

title View Course Use Case - Sequence Diagram

actor User
participant "Frontend\n(React/Vue)" as Frontend
participant "AuthController" as Auth
participant "CourseController" as CourseCtrl
participant "CourseService" as CourseService
participant "AssignmentService" as AssignmentService
participant "LessonService" as LessonService
participant "SubmissionService" as SubmissionService
participant "EnrollmentService" as EnrollmentService
database "Database" as DB

== Authentication ==
User -> Frontend: Truy cập hệ thống
Frontend -> Auth: POST /auth/login
Auth -> DB: Validate credentials
DB -> Auth: User info + Role
Auth -> Frontend: JWT Token + User role
Frontend -> User: Dashboard hiển thị

== Course List Request ==
User -> Frontend: Chọn "Xem khóa học"
Frontend -> CourseCtrl: GET /courses (with Authorization header)
CourseCtrl -> CourseService: getCoursesByUserRole(userId, role)

alt User role is STUDENT
    CourseService -> EnrollmentService: getEnrolledCourses(userId)
    EnrollmentService -> DB: SELECT courses by enrollment
    DB -> EnrollmentService: Enrolled courses
    EnrollmentService -> CourseService: Course list
else User role is TEACHER
    CourseService -> DB: SELECT courses by teacher_id
    DB -> CourseService: Teacher's courses
else User role is ADMIN
    CourseService -> DB: SELECT all courses
    DB -> CourseService: All courses
end

CourseService -> CourseCtrl: Course list
CourseCtrl -> Frontend: HTTP 200 + Course list
Frontend -> User: Hiển thị danh sách khóa học

== Course Detail Request ==
User -> Frontend: Chọn khóa học cụ thể
Frontend -> CourseCtrl: GET /courses/{courseId}
CourseCtrl -> CourseService: getCourseDetail(courseId, userId, role)

CourseService -> DB: SELECT course by id
DB -> CourseService: Course basic info

== Authorization Check ==
CourseService -> EnrollmentService: checkAccess(courseId, userId, role)
EnrollmentService -> DB: Verify enrollment/ownership
DB -> EnrollmentService: Access granted/denied

alt Access Denied
    EnrollmentService -> CourseService: Access denied
    CourseService -> CourseCtrl: Unauthorized
    CourseCtrl -> Frontend: HTTP 403 Forbidden
    Frontend -> User: "Không có quyền truy cập"
else Access Granted
    EnrollmentService -> CourseService: Access granted
    
    == Fetch Related Data ==
    par
        CourseService -> AssignmentService: getAssignmentsByCourse(courseId)
        AssignmentService -> DB: SELECT assignments
        DB -> AssignmentService: Assignment list
    and
        CourseService -> LessonService: getLessonsByCourse(courseId)
        LessonService -> DB: SELECT lessons
        DB -> LessonService: Lesson list
    and
        alt User role is STUDENT
            CourseService -> SubmissionService: getMySubmissions(courseId, userId)
            SubmissionService -> DB: SELECT submissions by student
            DB -> SubmissionService: Student submissions
        else User role is TEACHER or ADMIN
            CourseService -> SubmissionService: getAllSubmissions(courseId)
            SubmissionService -> DB: SELECT all submissions
            DB -> SubmissionService: All submissions
        end
    and
        alt User role is TEACHER or ADMIN
            CourseService -> EnrollmentService: getEnrollmentStats(courseId)
            EnrollmentService -> DB: SELECT enrollment statistics
            DB -> EnrollmentService: Enrollment stats
        end
    end
    
    == Prepare Response Based on Role ==
    alt User role is STUDENT
        CourseService -> CourseService: prepareStudentView(courseData)
        note right: Filter data for student view:\n- Basic course info\n- Assignments and lessons\n- Own submissions only
    else User role is TEACHER
        CourseService -> CourseService: prepareTeacherView(courseData)
        note right: Prepare teacher view:\n- Full course management\n- All assignments and lessons\n- Student list and submissions\n- Grading interface
    else User role is ADMIN
        CourseService -> CourseService: prepareAdminView(courseData)
        note right: Prepare admin view:\n- Full system view\n- All statistical data\n- User management\n- System logs
    end
    
    CourseService -> CourseCtrl: Course detail + Related data
    CourseCtrl -> Frontend: HTTP 200 + Course detail
    Frontend -> User: Hiển thị thông tin chi tiết khóa học
end

== Additional Actions ==
loop User continues viewing
    alt User requests assignment detail
        User -> Frontend: Chọn assignment
        Frontend -> CourseCtrl: GET /assignments/{assignmentId}
        CourseCtrl -> AssignmentService: getAssignmentDetail(assignmentId, userId)
        AssignmentService -> DB: SELECT assignment detail
        DB -> AssignmentService: Assignment data
        AssignmentService -> CourseCtrl: Assignment detail
        CourseCtrl -> Frontend: HTTP 200 + Assignment detail
        Frontend -> User: Hiển thị chi tiết assignment
    
    else User requests lesson detail
        User -> Frontend: Chọn lesson
        Frontend -> CourseCtrl: GET /lessons/{lessonId}
        CourseCtrl -> LessonService: getLessonDetail(lessonId, userId)
        LessonService -> DB: SELECT lesson detail
        DB -> LessonService: Lesson data
        LessonService -> CourseCtrl: Lesson detail
        CourseCtrl -> Frontend: HTTP 200 + Lesson detail
        Frontend -> User: Hiển thị chi tiết lesson
        
    else User refreshes course info
        User -> Frontend: Refresh page
        Frontend -> CourseCtrl: GET /courses/{courseId} (refresh)
        note right: Repeat course detail flow
    end
end

@enduml
```

### Simplified Sequence Diagrams by Actor

#### 1. Student View Course Sequence
```plantuml
@startuml "Student View Course Sequence"
!theme plain

title Student - View Course Sequence

actor Student
participant "Frontend" as Frontend
participant "AuthController" as Auth
participant "CourseController" as CourseCtrl
participant "CourseService" as CourseService
participant "EnrollmentService" as EnrollmentService
participant "SubmissionService" as SubmissionService
database "Database" as DB

== Student Authentication ==
Student -> Frontend: Đăng nhập
Frontend -> Auth: POST /auth/login
Auth -> DB: Validate student credentials
DB -> Auth: Student user info
Auth -> Frontend: JWT Token (STUDENT role)

== Get Enrolled Courses ==
Student -> Frontend: Truy cập "My Courses"
Frontend -> CourseCtrl: GET /courses (Student token)
CourseCtrl -> CourseService: getCoursesByStudent(studentId)
CourseService -> EnrollmentService: getEnrolledCourses(studentId)
EnrollmentService -> DB: SELECT enrolled courses
DB -> EnrollmentService: Student's courses
EnrollmentService -> CourseService: Course list
CourseService -> CourseCtrl: Enrolled courses
CourseCtrl -> Frontend: HTTP 200 + Course list
Frontend -> Student: Hiển thị khóa học đã đăng ký

== View Specific Course ==
Student -> Frontend: Chọn khóa học
Frontend -> CourseCtrl: GET /courses/{courseId}
CourseCtrl -> CourseService: getCourseForStudent(courseId, studentId)

CourseService -> EnrollmentService: verifyEnrollment(courseId, studentId)
EnrollmentService -> DB: Check enrollment
DB -> EnrollmentService: Enrollment confirmed

par Get Course Data
    CourseService -> DB: SELECT course details
    DB -> CourseService: Course info
and Get Student Submissions
    CourseService -> SubmissionService: getStudentSubmissions(courseId, studentId)
    SubmissionService -> DB: SELECT student submissions
    DB -> SubmissionService: Submission status
end

CourseService -> CourseCtrl: Course info + Submission status
CourseCtrl -> Frontend: HTTP 200 + Course data
Frontend -> Student: Hiển thị thông tin khóa học và tiến độ

== Student Actions ==
loop Student interaction
    alt View Assignment
        Student -> Frontend: Chọn assignment
        Frontend -> CourseCtrl: GET /assignments/{assignmentId}
        CourseCtrl -> Frontend: Assignment detail
        Frontend -> Student: Hiển thị chi tiết assignment
    
    else Check Submission Status
        Student -> Frontend: Xem trạng thái nộp bài
        Frontend -> CourseCtrl: GET /submissions/my-status
        CourseCtrl -> SubmissionService: getSubmissionStatus(studentId, courseId)
        SubmissionService -> DB: SELECT submission status
        DB -> SubmissionService: Status data
        SubmissionService -> CourseCtrl: Submission status
        CourseCtrl -> Frontend: HTTP 200 + Status
        Frontend -> Student: Hiển thị trạng thái nộp bài
    end
end

@enduml
```

#### 2. Teacher View Course Sequence
```plantuml
@startuml "Teacher View Course Sequence"
!theme plain

title Teacher - View Course Sequence

actor Teacher
participant "Frontend" as Frontend
participant "AuthController" as Auth
participant "CourseController" as CourseCtrl
participant "CourseService" as CourseService
participant "AssignmentService" as AssignmentService
participant "SubmissionService" as SubmissionService
participant "EnrollmentService" as EnrollmentService
database "Database" as DB

== Teacher Authentication ==
Teacher -> Frontend: Đăng nhập
Frontend -> Auth: POST /auth/login
Auth -> DB: Validate teacher credentials
DB -> Auth: Teacher user info
Auth -> Frontend: JWT Token (TEACHER role)

== Get Teacher's Courses ==
Teacher -> Frontend: Truy cập "My Courses"
Frontend -> CourseCtrl: GET /courses (Teacher token)
CourseCtrl -> CourseService: getCoursesByTeacher(teacherId)
CourseService -> DB: SELECT courses by teacher_id
DB -> CourseService: Teacher's courses
CourseService -> CourseCtrl: Course list
CourseCtrl -> Frontend: HTTP 200 + Course list
Frontend -> Teacher: Hiển thị khóa học giảng dạy

== View Course for Management ==
Teacher -> Frontend: Chọn khóa học để quản lý
Frontend -> CourseCtrl: GET /courses/{courseId}/management
CourseCtrl -> CourseService: getCourseForTeacher(courseId, teacherId)

CourseService -> DB: Verify course ownership
DB -> CourseService: Ownership confirmed

par Get Course Management Data
    CourseService -> DB: SELECT course details
    DB -> CourseService: Course info
and Get Assignments
    CourseService -> AssignmentService: getAssignmentsByCourse(courseId)
    AssignmentService -> DB: SELECT assignments
    DB -> AssignmentService: Assignment list
and Get Students
    CourseService -> EnrollmentService: getEnrolledStudents(courseId)
    EnrollmentService -> DB: SELECT enrolled students
    DB -> EnrollmentService: Student list
and Get Submissions
    CourseService -> SubmissionService: getAllSubmissions(courseId)
    SubmissionService -> DB: SELECT all submissions
    DB -> SubmissionService: All submissions
end

CourseService -> CourseCtrl: Complete course management data
CourseCtrl -> Frontend: HTTP 200 + Management data
Frontend -> Teacher: Hiển thị giao diện quản lý khóa học

== Teacher Management Actions ==
loop Teacher interaction
    alt View Submission Details
        Teacher -> Frontend: Chọn submission để chấm
        Frontend -> CourseCtrl: GET /submissions/{submissionId}
        CourseCtrl -> SubmissionService: getSubmissionForGrading(submissionId)
        SubmissionService -> DB: SELECT submission details
        DB -> SubmissionService: Submission data
        SubmissionService -> CourseCtrl: Submission details
        CourseCtrl -> Frontend: HTTP 200 + Submission data
        Frontend -> Teacher: Hiển thị chi tiết submission
    
    else Get Statistics
        Teacher -> Frontend: Xem thống kê
        Frontend -> CourseCtrl: GET /courses/{courseId}/statistics
        CourseCtrl -> CourseService: getCourseStatistics(courseId)
        CourseService -> SubmissionService: getSubmissionStatistics(courseId)
        SubmissionService -> DB: SELECT submission stats
        DB -> SubmissionService: Statistics data
        SubmissionService -> CourseService: Stats
        CourseService -> CourseCtrl: Course statistics
        CourseCtrl -> Frontend: HTTP 200 + Statistics
        Frontend -> Teacher: Hiển thị thống kê khóa học
    end
end

@enduml
```

#### 3. Admin View Course Sequence
```plantuml
@startuml "Admin View Course Sequence"
!theme plain

title Admin - View Course Sequence

actor Admin
participant "Frontend" as Frontend
participant "AuthController" as Auth
participant "CourseController" as CourseCtrl
participant "CourseService" as CourseService
participant "AdminService" as AdminService
participant "AuditService" as AuditService
database "Database" as DB

== Admin Authentication ==
Admin -> Frontend: Đăng nhập với quyền Admin
Frontend -> Auth: POST /auth/login
Auth -> DB: Validate admin credentials
DB -> Auth: Admin user info
Auth -> Frontend: JWT Token (ADMIN role)

== Get All Courses ==
Admin -> Frontend: Truy cập "Course Management"
Frontend -> CourseCtrl: GET /admin/courses
CourseCtrl -> CourseService: getAllCoursesForAdmin()
CourseService -> DB: SELECT all courses with metadata
DB -> CourseService: All courses + statistics
CourseService -> CourseCtrl: Complete course list
CourseCtrl -> Frontend: HTTP 200 + Course list
Frontend -> Admin: Hiển thị tất cả khóa học với thống kê

== View Course for Administration ==
Admin -> Frontend: Chọn khóa học để quản trị
Frontend -> CourseCtrl: GET /admin/courses/{courseId}
CourseCtrl -> CourseService: getCourseForAdmin(courseId)

par Get Complete Course Data
    CourseService -> DB: SELECT course details
    DB -> CourseService: Course info
and Get Administrative Data
    CourseService -> AdminService: getAdminCourseData(courseId)
    AdminService -> DB: SELECT admin-specific data
    DB -> AdminService: Admin data
and Get Audit Trail
    CourseService -> AuditService: getCourseAuditTrail(courseId)
    AuditService -> DB: SELECT audit logs
    DB -> AuditService: Audit trail
and Get System Statistics
    CourseService -> AdminService: getSystemStatistics(courseId)
    AdminService -> DB: SELECT system stats
    DB -> AdminService: System statistics
end

CourseService -> CourseCtrl: Complete administrative data
CourseCtrl -> Frontend: HTTP 200 + Admin data
Frontend -> Admin: Hiển thị giao diện quản trị khóa học

== Admin Management Actions ==
loop Admin interaction
    alt View System Logs
        Admin -> Frontend: Xem system logs
        Frontend -> CourseCtrl: GET /admin/courses/{courseId}/logs
        CourseCtrl -> AuditService: getSystemLogs(courseId)
        AuditService -> DB: SELECT system logs
        DB -> AuditService: Log data
        AuditService -> CourseCtrl: System logs
        CourseCtrl -> Frontend: HTTP 200 + Logs
        Frontend -> Admin: Hiển thị system logs
    
    else Generate Reports
        Admin -> Frontend: Tạo báo cáo
        Frontend -> CourseCtrl: POST /admin/courses/{courseId}/reports
        CourseCtrl -> AdminService: generateCourseReport(courseId)
        AdminService -> DB: SELECT comprehensive data
        DB -> AdminService: Report data
        AdminService -> CourseCtrl: Generated report
        CourseCtrl -> Frontend: HTTP 200 + Report
        Frontend -> Admin: Hiển thị/Download báo cáo
    
    else Manage Enrollments
        Admin -> Frontend: Quản lý đăng ký
        Frontend -> CourseCtrl: GET /admin/courses/{courseId}/enrollments
        CourseCtrl -> AdminService: getEnrollmentManagement(courseId)
        AdminService -> DB: SELECT enrollment data
        DB -> AdminService: Enrollment info
        AdminService -> CourseCtrl: Enrollment management data
        CourseCtrl -> Frontend: HTTP 200 + Enrollment data
        Frontend -> Admin: Hiển thị quản lý đăng ký
    end
end

@enduml
```

### API Endpoints Summary

#### Course Controller Endpoints
- `GET /courses` - Get courses by user role
- `GET /courses/{courseId}` - Get course detail
- `GET /courses/{courseId}/management` - Teacher management view
- `GET /admin/courses` - Admin view all courses
- `GET /admin/courses/{courseId}` - Admin course management
- `GET /courses/{courseId}/statistics` - Course statistics
- `GET /admin/courses/{courseId}/logs` - System logs
- `POST /admin/courses/{courseId}/reports` - Generate reports

#### Service Layer Interactions
- **CourseService**: Core course business logic
- **EnrollmentService**: Handle enrollment verification
- **SubmissionService**: Manage submission data
- **AssignmentService**: Handle assignment data
- **LessonService**: Manage lesson data
- **AdminService**: Administrative functions
- **AuditService**: Audit trail and logging

### Security & Authorization

1. **JWT Token**: All requests require valid JWT token
2. **Role-Based Access**: Different data based on user role
3. **Enrollment Verification**: Students can only view enrolled courses
4. **Ownership Verification**: Teachers can only view their courses
5. **Admin Privileges**: Full system access for admin users

### Error Handling

- **HTTP 401**: Unauthorized (invalid token)
- **HTTP 403**: Forbidden (insufficient permissions)
- **HTTP 404**: Course not found
- **HTTP 500**: Internal server error

### Performance Considerations

- **Parallel Processing**: Multiple service calls in parallel
- **Lazy Loading**: Load additional data on demand
- **Caching**: Cache frequently accessed course data
- **Pagination**: For large course lists

## Usage Notes

1. **PlantUML**: Copy từng section riêng biệt để tránh lỗi
2. **Sequence Complexity**: Có thể chia nhỏ thành multiple diagrams
3. **Database Optimization**: Consider caching for frequently accessed data
4. **Error Flows**: Add error handling sequences if needed
