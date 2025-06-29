# Use Case Diagrams for LMS (Learning Management System)

## System Overview Analysis
Based on the source code analysis, this LMS system supports the following main actors:
- **Student** (Sinh viên)
- **Teacher** (Giáo viên) 
- **Department Head** (Trưởng khoa)
- **Admin** (Quản trị viên)
- **Class Advisor** (Cố vấn học tập)

## Main Use Case Diagram

```plantuml
@startuml "LMS Main Use Cases"
!theme plain

' Actors
actor Student as "👨‍🎓\nStudent\n(Sinh viên)"
actor Teacher as "👩‍🏫\nTeacher\n(Giáo viên)"
actor DeptHead as "👨‍💼\nDepartment Head\n(Trưởng khoa)"
actor Admin as "🔧\nAdmin\n(Quản trị viên)"
actor Advisor as "👨‍🏫\nClass Advisor\n(Cố vấn học tập)"

' System boundary
rectangle "LMS System" {
    
    ' Authentication Use Cases
    package "Authentication" {
        usecase (Login) as UC_Login
        usecase (Logout) as UC_Logout
        usecase (Refresh Token) as UC_Refresh
        usecase (Get Account Info) as UC_Account
    }
    
    ' Course Management
    package "Course Management" {
        usecase (View Courses) as UC_ViewCourses
        usecase (Create Course) as UC_CreateCourse
        usecase (Enroll in Course) as UC_Enroll
        usecase (Manage Enrollments) as UC_ManageEnroll
    }
    
    ' Assignment Management
    package "Assignment Management" {
        usecase (Create Assignment) as UC_CreateAssignment
        usecase (View Assignments) as UC_ViewAssignments
        usecase (Update Assignment) as UC_UpdateAssignment
        usecase (Delete Assignment) as UC_DeleteAssignment
        usecase (Publish Assignment) as UC_PublishAssignment
        usecase (Add Assignment Files) as UC_AddAssignmentFiles
        usecase (Comment on Assignment) as UC_CommentAssignment
    }
    
    ' Submission Management
    package "Submission Management" {
        usecase (Submit Assignment) as UC_Submit
        usecase (Update Submission) as UC_UpdateSubmission
        usecase (View My Submissions) as UC_ViewMySubmissions
        usecase (Delete Submission) as UC_DeleteSubmission
        usecase (Grade Submission) as UC_GradeSubmission
        usecase (View All Submissions) as UC_ViewAllSubmissions
        usecase (Get Submission Statistics) as UC_SubmissionStats
        usecase (Check Submission Status) as UC_CheckSubmission
        usecase (Get Unsubmitted Count) as UC_UnsubmittedCount
    }
    
    ' Lesson Management
    package "Lesson Management" {
        usecase (Create Lesson) as UC_CreateLesson
        usecase (View Lessons) as UC_ViewLessons
        usecase (Update Lesson) as UC_UpdateLesson
        usecase (Delete Lesson) as UC_DeleteLesson
        usecase (Publish Lesson) as UC_PublishLesson
        usecase (Add Lesson Files) as UC_AddLessonFiles
    }
    
    ' Auto Grading System
    package "Auto Grading" {
        usecase (Auto Grade Overdue) as UC_AutoGrade
        usecase (Monitor Auto Grading) as UC_MonitorAutoGrade
    }
}

' Student relationships
Student --> UC_Login
Student --> UC_Logout
Student --> UC_Refresh
Student --> UC_Account
Student --> UC_ViewCourses
Student --> UC_Enroll
Student --> UC_ViewAssignments
Student --> UC_Submit
Student --> UC_UpdateSubmission
Student --> UC_ViewMySubmissions
Student --> UC_DeleteSubmission
Student --> UC_CheckSubmission
Student --> UC_ViewLessons
Student --> UC_UnsubmittedCount

' Teacher relationships
Teacher --> UC_Login
Teacher --> UC_Logout
Teacher --> UC_Refresh
Teacher --> UC_Account
Teacher --> UC_CreateCourse
Teacher --> UC_ViewCourses
Teacher --> UC_ManageEnroll
Teacher --> UC_CreateAssignment
Teacher --> UC_ViewAssignments
Teacher --> UC_UpdateAssignment
Teacher --> UC_DeleteAssignment
Teacher --> UC_PublishAssignment
Teacher --> UC_AddAssignmentFiles
Teacher --> UC_CommentAssignment
Teacher --> UC_GradeSubmission
Teacher --> UC_ViewAllSubmissions
Teacher --> UC_SubmissionStats
Teacher --> UC_CreateLesson
Teacher --> UC_ViewLessons
Teacher --> UC_UpdateLesson
Teacher --> UC_DeleteLesson
Teacher --> UC_PublishLesson
Teacher --> UC_AddLessonFiles
Teacher --> UC_UnsubmittedCount

' Department Head relationships  
DeptHead --> UC_Login
DeptHead --> UC_Logout
DeptHead --> UC_ViewCourses
DeptHead --> UC_ViewAssignments
DeptHead --> UC_SubmissionStats
DeptHead --> UC_MonitorAutoGrade

' Admin relationships
Admin --> UC_Login
Admin --> UC_Logout
Admin --> UC_CreateCourse
Admin --> UC_ViewCourses
Admin --> UC_ManageEnroll
Admin --> UC_ViewAllSubmissions
Admin --> UC_SubmissionStats
Admin --> UC_MonitorAutoGrade

' Class Advisor relationships
Advisor --> UC_Login
Advisor --> UC_Logout
Advisor --> UC_ViewCourses
Advisor --> UC_ViewAssignments
Advisor --> UC_ViewAllSubmissions
Advisor --> UC_SubmissionStats
Advisor --> UC_UnsubmittedCount

' System relationships (auto grading)
UC_AutoGrade ..> UC_GradeSubmission : <<include>>
UC_MonitorAutoGrade ..> UC_AutoGrade : <<extend>>

@enduml
```

## Assignment Management Detailed Use Case Diagram

```plantuml
@startuml "Assignment Management Use Cases"
!theme plain

actor Teacher as "👩‍🏫\nTeacher"
actor Student as "👨‍🎓\nStudent"

rectangle "Assignment Management System" {
    
    package "Assignment CRUD" {
        usecase (Create Assignment) as UC_Create
        usecase (Create Assignment with Files) as UC_CreateWithFiles
        usecase (View Assignment Details) as UC_ViewDetails
        usecase (Update Assignment Info) as UC_UpdateInfo
        usecase (Update Assignment with Files) as UC_UpdateWithFiles
        usecase (Delete Assignment) as UC_Delete
        usecase (Publish Assignment) as UC_Publish
    }
    
    package "Assignment Interaction" {
        usecase (Add Comment) as UC_Comment
        usecase (View Comments) as UC_ViewComments
        usecase (Get Assignment Count) as UC_Count
        usecase (Filter Assignments) as UC_Filter
    }
    
    package "Assignment Files" {
        usecase (Upload Files) as UC_Upload
        usecase (Download Files) as UC_Download
        usecase (Delete Files) as UC_DeleteFiles
    }
}

' Teacher relationships
Teacher --> UC_Create
Teacher --> UC_CreateWithFiles
Teacher --> UC_ViewDetails
Teacher --> UC_UpdateInfo
Teacher --> UC_UpdateWithFiles
Teacher --> UC_Delete
Teacher --> UC_Publish
Teacher --> UC_Comment
Teacher --> UC_ViewComments
Teacher --> UC_Count
Teacher --> UC_Filter
Teacher --> UC_Upload
Teacher --> UC_Download
Teacher --> UC_DeleteFiles

' Student relationships
Student --> UC_ViewDetails
Student --> UC_ViewComments
Student --> UC_Filter
Student --> UC_Download

' Include relationships
UC_CreateWithFiles ..> UC_Upload : <<include>>
UC_UpdateWithFiles ..> UC_Upload : <<include>>
UC_Create ..> UC_Publish : <<extend>>
UC_UpdateInfo ..> UC_Publish : <<extend>>

@enduml
```

## Submission Management Detailed Use Case Diagram

```plantuml
@startuml "Submission Management Use Cases"
!theme plain

actor Student as "👨‍🎓\nStudent"
actor Teacher as "👩‍🏫\nTeacher"
actor System as "🤖\nAuto Grading\nSystem"

rectangle "Submission Management System" {
    
    package "Student Submission" {
        usecase (Submit Assignment) as UC_Submit
        usecase (Submit or Update) as UC_SubmitOrUpdate
        usecase (Update Submission) as UC_Update
        usecase (Delete Submission) as UC_Delete
        usecase (View My Submissions) as UC_ViewMine
        usecase (Check Submission Status) as UC_CheckStatus
        usecase (Get Unsubmitted Count) as UC_UnsubmittedCount
    }
    
    package "Teacher Grading" {
        usecase (Grade Submission) as UC_Grade
        usecase (View All Submissions) as UC_ViewAll
        usecase (Get Submission Statistics) as UC_Stats
        usecase (View Submission Details) as UC_ViewDetails
    }
    
    package "Auto Grading" {
        usecase (Auto Grade Overdue) as UC_AutoGrade
        usecase (Create Zero Submissions) as UC_CreateZero
        usecase (Check Overdue Assignments) as UC_CheckOverdue
    }
    
    package "File Management" {
        usecase (Upload Submission Files) as UC_UploadFiles
        usecase (Download Submission Files) as UC_DownloadFiles
        usecase (Delete Submission Files) as UC_DeleteFiles
    }
}

' Student relationships
Student --> UC_Submit
Student --> UC_SubmitOrUpdate
Student --> UC_Update
Student --> UC_Delete
Student --> UC_ViewMine
Student --> UC_CheckStatus
Student --> UC_UploadFiles
Student --> UC_DownloadFiles

' Teacher relationships
Teacher --> UC_Grade
Teacher --> UC_ViewAll
Teacher --> UC_Stats
Teacher --> UC_ViewDetails
Teacher --> UC_UnsubmittedCount
Teacher --> UC_DownloadFiles

' System relationships
System --> UC_AutoGrade
System --> UC_CheckOverdue

' Include relationships
UC_Submit ..> UC_UploadFiles : <<include>>
UC_Update ..> UC_UploadFiles : <<include>>
UC_SubmitOrUpdate ..> UC_Submit : <<extend>>
UC_SubmitOrUpdate ..> UC_Update : <<extend>>
UC_AutoGrade ..> UC_CreateZero : <<include>>
UC_AutoGrade ..> UC_CheckOverdue : <<include>>

@enduml
```

## User Authentication & Authorization Use Case Diagram

```plantuml
@startuml "Authentication Use Cases"
!theme plain

actor User as "👤\nUser"
actor Student as "👨‍🎓\nStudent"
actor Teacher as "👩‍🏫\nTeacher"
actor Admin as "🔧\nAdmin"

rectangle "Authentication System" {
    
    package "Core Authentication" {
        usecase (Login) as UC_Login
        usecase (Logout) as UC_Logout
        usecase (Refresh Token) as UC_Refresh
        usecase (Get Account Info) as UC_Account
    }
    
    package "Session Management" {
        usecase (Validate Token) as UC_Validate
        usecase (Generate Token) as UC_Generate
        usecase (Revoke Token) as UC_Revoke
    }
    
    package "Authorization" {
        usecase (Check Student Permission) as UC_StudentPerm
        usecase (Check Teacher Permission) as UC_TeacherPerm
        usecase (Check Admin Permission) as UC_AdminPerm
        usecase (Role-based Access Control) as UC_RBAC
    }
}

' User relationships
User --> UC_Login
User --> UC_Logout
User --> UC_Refresh
User --> UC_Account

Student --|> User
Teacher --|> User
Admin --|> User

' Internal system relationships
UC_Login ..> UC_Generate : <<include>>
UC_Login ..> UC_RBAC : <<include>>
UC_Refresh ..> UC_Validate : <<include>>
UC_Refresh ..> UC_Generate : <<include>>
UC_Logout ..> UC_Revoke : <<include>>

UC_RBAC ..> UC_StudentPerm : <<extend>>
UC_RBAC ..> UC_TeacherPerm : <<extend>>
UC_RBAC ..> UC_AdminPerm : <<extend>>

@enduml
```

## Course & Enrollment Management Use Case Diagram

```plantuml
@startuml "Course Management Use Cases"
!theme plain

actor Student as "👨‍🎓\nStudent"
actor Teacher as "👩‍🏫\nTeacher"
actor Admin as "🔧\nAdmin"
actor DeptHead as "👨‍💼\nDepartment Head"

rectangle "Course Management System" {
    
    package "Course Operations" {
        usecase (Create Course) as UC_CreateCourse
        usecase (View Courses) as UC_ViewCourses
        usecase (Update Course) as UC_UpdateCourse
        usecase (Delete Course) as UC_DeleteCourse
        usecase (Get Course Statistics) as UC_CourseStats
    }
    
    package "Enrollment Management" {
        usecase (Enroll in Course) as UC_Enroll
        usecase (Drop from Course) as UC_Drop
        usecase (View Enrollments) as UC_ViewEnrollments
        usecase (Manage Class List) as UC_ManageClass
        usecase (View Student List) as UC_ViewStudents
    }
    
    package "Course Content" {
        usecase (View Course Details) as UC_CourseDetails
        usecase (Access Course Materials) as UC_Materials
        usecase (View Course Schedule) as UC_Schedule
    }
}

' Student relationships
Student --> UC_ViewCourses
Student --> UC_Enroll
Student --> UC_Drop
Student --> UC_ViewEnrollments
Student --> UC_CourseDetails
Student --> UC_Materials
Student --> UC_Schedule

' Teacher relationships
Teacher --> UC_CreateCourse
Teacher --> UC_ViewCourses
Teacher --> UC_UpdateCourse
Teacher --> UC_ManageClass
Teacher --> UC_ViewStudents
Teacher --> UC_CourseDetails
Teacher --> UC_CourseStats

' Admin relationships
Admin --> UC_CreateCourse
Admin --> UC_ViewCourses
Admin --> UC_UpdateCourse
Admin --> UC_DeleteCourse
Admin --> UC_ViewEnrollments
Admin --> UC_ManageClass
Admin --> UC_CourseStats

' Department Head relationships
DeptHead --> UC_ViewCourses
DeptHead --> UC_ViewEnrollments
DeptHead --> UC_CourseStats
DeptHead --> UC_ViewStudents

@enduml
```

## Lesson Management Use Case Diagram

```plantuml
@startuml "Lesson Management Use Cases"
!theme plain

actor Student as "👨‍🎓\nStudent"
actor Teacher as "👩‍🏫\nTeacher"

rectangle "Lesson Management System" {
    
    package "Lesson CRUD" {
        usecase (Create Lesson) as UC_Create
        usecase (Create Lesson with Files) as UC_CreateWithFiles
        usecase (View Lesson) as UC_View
        usecase (Update Lesson) as UC_Update
        usecase (Update Lesson with Files) as UC_UpdateWithFiles
        usecase (Delete Lesson) as UC_Delete
        usecase (Publish Lesson) as UC_Publish
    }
    
    package "Lesson Content" {
        usecase (Upload Lesson Materials) as UC_Upload
        usecase (Download Materials) as UC_Download
        usecase (View Lesson Content) as UC_ViewContent
        usecase (Search Lessons) as UC_Search
    }
    
    package "Lesson Organization" {
        usecase (Order Lessons) as UC_Order
        usecase (Categorize Lessons) as UC_Categorize
        usecase (Filter Lessons) as UC_Filter
    }
}

' Teacher relationships
Teacher --> UC_Create
Teacher --> UC_CreateWithFiles
Teacher --> UC_View
Teacher --> UC_Update
Teacher --> UC_UpdateWithFiles
Teacher --> UC_Delete
Teacher --> UC_Publish
Teacher --> UC_Upload
Teacher --> UC_Download
Teacher --> UC_Order
Teacher --> UC_Categorize

' Student relationships
Student --> UC_View
Student --> UC_ViewContent
Student --> UC_Download
Student --> UC_Search
Student --> UC_Filter

' Include relationships
UC_CreateWithFiles ..> UC_Upload : <<include>>
UC_UpdateWithFiles ..> UC_Upload : <<include>>

@enduml
```

## System Integration & Monitoring Use Case Diagram

```plantuml
@startuml "System Integration Use Cases"
!theme plain

actor Admin as "🔧\nAdmin"
actor Teacher as "👩‍🏫\nTeacher"
actor System as "🤖\nScheduled\nSystem"

rectangle "System Integration & Monitoring" {
    
    package "Auto Grading System" {
        usecase (Schedule Auto Grading) as UC_Schedule
        usecase (Process Overdue Assignments) as UC_ProcessOverdue
        usecase (Create Zero Submissions) as UC_CreateZero
        usecase (Monitor Auto Grading) as UC_Monitor
        usecase (Generate Auto Grade Reports) as UC_Reports
    }
    
    package "File Management" {
        usecase (Upload to Cloudinary) as UC_CloudUpload
        usecase (Download from Cloudinary) as UC_CloudDownload
        usecase (Delete from Cloudinary) as UC_CloudDelete
        usecase (Manage File Storage) as UC_FileStorage
    }
    
    package "Statistics & Reporting" {
        usecase (Generate Course Statistics) as UC_CourseStats
        usecase (Generate Submission Statistics) as UC_SubmissionStats
        usecase (Monitor System Performance) as UC_Performance
        usecase (Track User Activity) as UC_Activity
    }
    
    package "Security & Access Control" {
        usecase (Manage User Roles) as UC_Roles
        usecase (Control Access Permissions) as UC_Permissions
        usecase (Monitor Security) as UC_Security
        usecase (Audit System Logs) as UC_Audit
    }
}

' System relationships
System --> UC_Schedule
System --> UC_ProcessOverdue

' Admin relationships
Admin --> UC_Monitor
Admin --> UC_Reports
Admin --> UC_FileStorage
Admin --> UC_CourseStats
Admin --> UC_SubmissionStats
Admin --> UC_Performance
Admin --> UC_Activity
Admin --> UC_Roles
Admin --> UC_Permissions
Admin --> UC_Security
Admin --> UC_Audit

' Teacher relationships
Teacher --> UC_Monitor
Teacher --> UC_CourseStats
Teacher --> UC_SubmissionStats

' Include relationships
UC_Schedule ..> UC_ProcessOverdue : <<include>>
UC_ProcessOverdue ..> UC_CreateZero : <<include>>
UC_FileStorage ..> UC_CloudUpload : <<include>>
UC_FileStorage ..> UC_CloudDownload : <<include>>
UC_FileStorage ..> UC_CloudDelete : <<include>>

@enduml
```

## Use Case Descriptions

### Core Use Cases

#### UC_Login - User Login
**Actors**: All Users  
**Description**: Authenticate user with username/password and generate JWT token  
**Preconditions**: User has valid credentials  
**Postconditions**: User is authenticated and receives access token  

#### UC_Submit - Submit Assignment
**Actors**: Student  
**Description**: Student submits assignment with optional file attachments  
**Preconditions**: Student is enrolled in course, assignment is published  
**Postconditions**: Submission is created and stored in system  

#### UC_GradeSubmission - Grade Student Submission
**Actors**: Teacher  
**Description**: Teacher reviews and grades student submission  
**Preconditions**: Student has submitted assignment, teacher owns the course  
**Postconditions**: Submission is graded with score and feedback  

#### UC_AutoGrade - Automatic Grading
**Actors**: System  
**Description**: System automatically assigns zero score to overdue unsubmitted assignments  
**Preconditions**: Assignment is overdue and doesn't allow late submission  
**Postconditions**: Zero submissions created for students who didn't submit  

#### UC_CreateAssignment - Create Assignment
**Actors**: Teacher  
**Description**: Teacher creates new assignment for course  
**Preconditions**: Teacher owns the course  
**Postconditions**: Assignment is created and can be published  

### Advanced Use Cases

#### UC_SubmitOrUpdate - Unified Submission
**Actors**: Student  
**Description**: Automatically determines whether to create new or update existing submission  
**Preconditions**: Student is enrolled in course  
**Postconditions**: Submission is created or updated appropriately  

#### UC_UnsubmittedCount - Get Unsubmitted Assignment Count
**Actors**: Teacher, Student, Advisor  
**Description**: Get count of assignments student can still submit  
**Preconditions**: Valid student ID provided  
**Postconditions**: Returns count of submittable assignments  

#### UC_SubmissionStats - Submission Statistics
**Actors**: Teacher, Admin, Department Head  
**Description**: Get comprehensive statistics about assignment submissions  
**Preconditions**: User has permission to view statistics  
**Postconditions**: Returns detailed submission analytics  

---

These use case diagrams provide a comprehensive view of the LMS system functionality based on the source code analysis. The system supports complex workflows for course management, assignment handling, submission processing, and automated grading.
