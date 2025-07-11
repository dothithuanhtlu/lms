package vn.doan.lms.domain.enums;

import org.springframework.http.HttpMethod;

/**
 * Enum định nghĩa tất cả các quyền trong hệ thống LMS
 * Mỗi quyền bao gồm: tên, HTTP method, endpoint pattern và mô tả
 */
public enum Permission {

        // ================================
        // USER MANAGEMENT PERMISSIONS
        // ================================
        CREATE_USER("CREATE_USER", HttpMethod.POST, "/admin/user", "Tạo người dùng mới"),
        VIEW_ALL_USERS("VIEW_ALL_USERS", HttpMethod.GET, "/admin/users", "Xem danh sách tất cả người dùng"),
        VIEW_USER_STATISTICS("VIEW_USER_STATISTICS", HttpMethod.GET, "/admin/users/statistics",
                        "Xem thống kê người dùng"),
        UPDATE_USER("UPDATE_USER", HttpMethod.PUT, "/admin/users/{userCode}", "Cập nhật thông tin người dùng"),
        DELETE_USER("DELETE_USER", HttpMethod.DELETE, "/admin/user/{userCode}", "Xóa người dùng"),

        // User details by role
        VIEW_STUDENT_DETAILS("VIEW_STUDENT_DETAILS", HttpMethod.GET, "/admin/student/{userCode}",
                        "Xem chi tiết sinh viên"),
        VIEW_TEACHER_DETAILS("VIEW_TEACHER_DETAILS", HttpMethod.GET, "/admin/teacher/{userCode}",
                        "Xem chi tiết giáo viên"),
        VIEW_ADMIN_DETAILS("VIEW_ADMIN_DETAILS", HttpMethod.GET, "/admin/admin/{userCode}", "Xem chi tiết admin"),

        // ================================
        // COURSE MANAGEMENT PERMISSIONS
        // ================================
        CREATE_COURSE("CREATE_COURSE", HttpMethod.POST, "/admin/courses", "Tạo khóa học mới"),
        VIEW_ALL_COURSES("VIEW_ALL_COURSES", HttpMethod.GET, "/admin/courses", "Xem danh sách tất cả khóa học"),
        VIEW_COURSE_INFO("VIEW_COURSE_INFO", HttpMethod.GET, "/admin/courses/info", "Xem thông tin thống kê khóa học"),
        VIEW_COURSE_DETAILS("VIEW_COURSE_DETAILS", HttpMethod.GET, "/admin/courses/{courseId}/details",
                        "Xem chi tiết khóa học"),
        VIEW_COURSE_FULL_DETAILS("VIEW_COURSE_FULL_DETAILS", HttpMethod.GET, "/admin/courses/{courseId}/full-details",
                        "Xem chi tiết đầy đủ khóa học"),
        UPDATE_COURSE("UPDATE_COURSE", HttpMethod.PUT, "/admin/courses/{courseId}", "Cập nhật khóa học"),
        DELETE_COURSE("DELETE_COURSE", HttpMethod.DELETE, "/admin/courses/{courseId}", "Xóa khóa học"),

        // Course access by role
        VIEW_COURSES_BY_TEACHER("VIEW_COURSES_BY_TEACHER", HttpMethod.GET, "/admin/courses/byteacher/{teacherId}",
                        "Xem khóa học theo giáo viên"),
        VIEW_COURSES_BY_STUDENT("VIEW_COURSES_BY_STUDENT", HttpMethod.GET, "/admin/courses/bystudent/{studentId}",
                        "Xem khóa học theo sinh viên"),
        VIEW_STUDENT_COURSE_DETAILS("VIEW_STUDENT_COURSE_DETAILS", HttpMethod.GET,
                        "/admin/student/courses/{courseId}/details", "Xem chi tiết khóa học cho sinh viên"),

        // Student scores
        VIEW_STUDENT_SCORES("VIEW_STUDENT_SCORES", HttpMethod.GET,
                        "/admin/courses/{courseId}/students/{studentId}/scores",
                        "Xem điểm số sinh viên"),
        UPDATE_STUDENT_SCORES("UPDATE_STUDENT_SCORES", HttpMethod.POST,
                        "/admin/courses/{courseId}/students/{studentId}/scores", "Cập nhật điểm số sinh viên"),

        // ================================
        // LESSON MANAGEMENT PERMISSIONS
        // ================================
        CREATE_LESSON("CREATE_LESSON", HttpMethod.POST, "/api/lessons/create", "Tạo bài học mới"),
        VIEW_LESSON("VIEW_LESSON", HttpMethod.GET, "/api/lessons/{lessonId}", "Xem chi tiết bài học"),
        UPDATE_LESSON("UPDATE_LESSON", HttpMethod.PUT, "/api/lessons/{lessonId}", "Cập nhật bài học"),
        UPDATE_LESSON_WITH_FILES("UPDATE_LESSON_WITH_FILES", HttpMethod.PUT,
                        "/api/lessons/{lessonId}/update-with-files",
                        "Cập nhật bài học với file"),
        PUBLISH_LESSON("PUBLISH_LESSON", HttpMethod.PUT, "/api/lessons/{lessonId}/publish", "Xuất bản bài học"),
        UPLOAD_LESSON_DOCUMENTS("UPLOAD_LESSON_DOCUMENTS", HttpMethod.POST, "/api/lesson-documents/upload",
                        "Upload tài liệu bài học"),

        // ================================
        // ASSIGNMENT MANAGEMENT PERMISSIONS
        // ================================
        CREATE_ASSIGNMENT("CREATE_ASSIGNMENT", HttpMethod.POST, "/api/assignments/create-with-files",
                        "Tạo bài tập mới"),
        VIEW_ASSIGNMENT("VIEW_ASSIGNMENT", HttpMethod.GET, "/api/assignments/detail/{assignmentId}",
                        "Xem chi tiết bài tập"),
        UPDATE_ASSIGNMENT("UPDATE_ASSIGNMENT", HttpMethod.PUT, "/api/assignments/update/{assignmentId}",
                        "Cập nhật bài tập"),
        UPDATE_ASSIGNMENT_WITH_FILES("UPDATE_ASSIGNMENT_WITH_FILES", HttpMethod.PUT,
                        "/api/assignments/{assignmentId}/update-with-files", "Cập nhật bài tập với file"),
        PUBLISH_ASSIGNMENT("PUBLISH_ASSIGNMENT", HttpMethod.PUT, "/api/assignments/{assignmentId}/publish",
                        "Xuất bản bài tập"),
        DELETE_ASSIGNMENT("DELETE_ASSIGNMENT", HttpMethod.DELETE, "/api/assignments/delete/{assignmentId}",
                        "Xóa bài tập"),

        // ================================
        // SUBMISSION MANAGEMENT PERMISSIONS
        // ================================
        SUBMIT_ASSIGNMENT("SUBMIT_ASSIGNMENT", HttpMethod.POST, "/api/submissions/submit-or-update", "Nộp bài tập"),
        VIEW_ASSIGNMENT_SUBMISSIONS("VIEW_ASSIGNMENT_SUBMISSIONS", HttpMethod.GET,
                        "/api/submissions/assignment/{assignmentId}", "Xem danh sách bài nộp"),
        GRADE_SUBMISSION("GRADE_SUBMISSION", HttpMethod.POST, "/api/submissions/{submissionId}/grade",
                        "Chấm điểm bài nộp"),
        VIEW_UNSUBMITTED_COUNT("VIEW_UNSUBMITTED_COUNT", HttpMethod.GET,
                        "/api/submissions/student/{studentId}/unsubmitted-count", "Xem số bài chưa nộp"),

        // ================================
        // ENROLLMENT MANAGEMENT PERMISSIONS
        // ================================
        CREATE_ENROLLMENT("CREATE_ENROLLMENT", HttpMethod.POST, "/enrollments", "Tạo đăng ký khóa học"),
        DELETE_ENROLLMENT("DELETE_ENROLLMENT", HttpMethod.DELETE, "/enrollments", "Xóa đăng ký khóa học"),

        // ================================
        // DEPARTMENT & SUBJECT PERMISSIONS
        // ================================
        VIEW_DEPARTMENTS("VIEW_DEPARTMENTS", HttpMethod.GET, "/departments/allresponses", "Xem danh sách phòng ban"),
        VIEW_DEPARTMENT_NAMES("VIEW_DEPARTMENT_NAMES", HttpMethod.GET, "/admin/departments/names", "Xem tên phòng ban"),
        VIEW_DEPARTMENT_NAME("VIEW_DEPARTMENT_NAME", HttpMethod.GET, "/admin/department/{departmentId}/name",
                        "Xem tên phòng ban theo ID"),
        VIEW_DEPARTMENT_TEACHERS("VIEW_DEPARTMENT_TEACHERS", HttpMethod.GET,
                        "/admin/department/{departmentId}/teachers_select", "Xem giáo viên theo phòng ban"),

        VIEW_SUBJECTS("VIEW_SUBJECTS", HttpMethod.GET, "/admin/subjects/{subjectId}", "Xem chi tiết môn học"),
        VIEW_SUBJECT_COURSES("VIEW_SUBJECT_COURSES", HttpMethod.GET, "/admin/subjects/{subjectId}/courses",
                        "Xem khóa học theo môn"),
        VIEW_SUBJECT_COURSES_PUBLIC("VIEW_SUBJECT_COURSES_PUBLIC", HttpMethod.GET, "/subjects/{subjectId}/courses",
                        "Xem khóa học theo môn (public)"),

        VIEW_MAJORS("VIEW_MAJORS", HttpMethod.GET, "/admin/majors/{departmentId}/allresponse",
                        "Xem chuyên ngành theo phòng ban"),
        VIEW_MAJOR_SUBJECTS("VIEW_MAJOR_SUBJECTS", HttpMethod.GET, "/admin/majors/{majorId}/subjects",
                        "Xem môn học theo chuyên ngành"),

        // ================================
        // CLASSROOM MANAGEMENT PERMISSIONS
        // ================================
        VIEW_CLASSROOM_NAMES("VIEW_CLASSROOM_NAMES", HttpMethod.GET, "/admin/classrooms/name", "Xem tên lớp học"),

        // ================================
        // DOCUMENT & FILE PERMISSIONS
        // ================================
        TEST_CLOUDINARY_STATUS("TEST_CLOUDINARY_STATUS", HttpMethod.GET, "/api/lesson-documents/test-cloudinary-status",
                        "Kiểm tra trạng thái Cloudinary"),
        CHECK_USAGE("CHECK_USAGE", HttpMethod.GET, "/api/lesson-documents/check-usage", "Kiểm tra usage"),
        ACCESS_STATIC_FILES("ACCESS_STATIC_FILES", HttpMethod.GET, "/{cleanPath}", "Truy cập file tĩnh"),

        // ================================
        // CHATBOT PERMISSIONS
        // ================================
        USE_CHATBOT("USE_CHATBOT", HttpMethod.GET, "/chatbot/{message}", "Sử dụng chatbot"),

        // ================================
        // AUTHENTICATION PERMISSIONS
        // ================================
        LOGIN("LOGIN", HttpMethod.POST, "/login", "Đăng nhập"),
        VIEW_ACCOUNT("VIEW_ACCOUNT", HttpMethod.GET, "/auth/account", "Xem thông tin tài khoản"),
        REFRESH_TOKEN("REFRESH_TOKEN", HttpMethod.GET, "/auth/refresh", "Làm mới token"),
        LOGOUT("LOGOUT", HttpMethod.POST, "/logout", "Đăng xuất"),

        // ================================
        // HEALTH CHECK PERMISSIONS
        // ================================
        HEALTH_CHECK("HEALTH_CHECK", HttpMethod.GET, "/health", "Kiểm tra sức khỏe hệ thống");

        private final String name;
        private final HttpMethod httpMethod;
        private final String endpointPattern;
        private final String description;

        Permission(String name, HttpMethod httpMethod, String endpointPattern, String description) {
                this.name = name;
                this.httpMethod = httpMethod;
                this.endpointPattern = endpointPattern;
                this.description = description;
        }

        // Getters
        public String getName() {
                return name;
        }

        public HttpMethod getHttpMethod() {
                return httpMethod;
        }

        public String getEndpointPattern() {
                return endpointPattern;
        }

        public String getDescription() {
                return description;
        }

        /**
         * Kiểm tra xem permission có khớp với request không
         * 
         * @param method HTTP method của request
         * @param path   Đường dẫn của request
         * @return true nếu khớp
         */
        public boolean matches(HttpMethod method, String path) {
                if (!this.httpMethod.equals(method)) {
                        return false;
                }

                // Chuyển đổi pattern thành regex
                String regex = this.endpointPattern
                                .replaceAll("\\{[^}]+\\}", "[^/]+") // {id} -> [^/]+
                                .replaceAll("\\*", ".*"); // * -> .*

                return path.matches(regex);
        }
}
