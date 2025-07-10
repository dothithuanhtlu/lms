package vn.doan.lms.domain.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enum định nghĩa phân quyền cho từng role trong hệ thống LMS
 * Mỗi role sẽ có danh sách các permission tương ứng
 */
public enum PermissionRole {

    /**
     * ADMIN - Quyền cao nhất, có thể truy cập tất cả chức năng
     */
    ADMIN(Arrays.asList(// User Management - Full Access
            Permission.CREATE_USER,
            Permission.VIEW_ALL_USERS,
            Permission.VIEW_USER_STATISTICS,
            Permission.UPDATE_USER,
            Permission.DELETE_USER,
            Permission.VIEW_STUDENT_DETAILS,
            Permission.VIEW_TEACHER_DETAILS,
            Permission.VIEW_ADMIN_DETAILS,

            // Course Management - Full Access
            Permission.CREATE_COURSE,
            Permission.VIEW_ALL_COURSES,
            Permission.VIEW_COURSE_INFO,
            Permission.VIEW_COURSE_DETAILS,
            Permission.VIEW_COURSE_FULL_DETAILS,
            Permission.UPDATE_COURSE,
            Permission.DELETE_COURSE,
            Permission.VIEW_COURSES_BY_TEACHER,
            Permission.VIEW_COURSES_BY_STUDENT,
            Permission.VIEW_STUDENT_COURSE_DETAILS,
            Permission.VIEW_STUDENT_SCORES,
            Permission.UPDATE_STUDENT_SCORES,

            // Lesson Management - Full Access
            Permission.CREATE_LESSON,
            Permission.VIEW_LESSON,
            Permission.UPDATE_LESSON,
            Permission.UPDATE_LESSON_WITH_FILES,
            Permission.PUBLISH_LESSON,
            Permission.UPLOAD_LESSON_DOCUMENTS,

            // Assignment Management - Full Access
            Permission.CREATE_ASSIGNMENT,
            Permission.VIEW_ASSIGNMENT,
            Permission.UPDATE_ASSIGNMENT,
            Permission.UPDATE_ASSIGNMENT_WITH_FILES,
            Permission.PUBLISH_ASSIGNMENT,
            Permission.DELETE_ASSIGNMENT,

            // Submission Management - Full Access
            Permission.VIEW_ASSIGNMENT_SUBMISSIONS,
            Permission.GRADE_SUBMISSION,
            Permission.VIEW_UNSUBMITTED_COUNT,

            // Enrollment Management
            Permission.CREATE_ENROLLMENT,
            Permission.DELETE_ENROLLMENT,

            // Department & Subject - Full Access
            Permission.VIEW_DEPARTMENTS,
            Permission.VIEW_DEPARTMENT_NAMES,
            Permission.VIEW_DEPARTMENT_NAME,
            Permission.VIEW_DEPARTMENT_TEACHERS,
            Permission.VIEW_SUBJECTS,
            Permission.VIEW_SUBJECT_COURSES,
            Permission.VIEW_SUBJECT_COURSES_PUBLIC,
            Permission.VIEW_MAJORS,
            Permission.VIEW_MAJOR_SUBJECTS,

            // Classroom Management
            Permission.VIEW_CLASSROOM_NAMES,

            // Document & File Access
            Permission.TEST_CLOUDINARY_STATUS,
            Permission.CHECK_USAGE,
            Permission.ACCESS_STATIC_FILES,

            // Chatbot
            Permission.USE_CHATBOT,

            // Authentication & Health
            Permission.LOGIN,
            Permission.VIEW_ACCOUNT,
            Permission.REFRESH_TOKEN,
            Permission.LOGOUT,
            Permission.HEALTH_CHECK)),

    /**
     * TEACHER - Quyền giáo viên, quản lý khóa học và bài giảng
     */
    TEACHER(Arrays.asList(// Course Access - Limited to own courses
            Permission.VIEW_COURSES_BY_TEACHER,
            Permission.VIEW_COURSE_FULL_DETAILS,
            Permission.VIEW_STUDENT_SCORES,
            Permission.UPDATE_STUDENT_SCORES,

            // Lesson Management - Full Access
            Permission.CREATE_LESSON,
            Permission.VIEW_LESSON,
            Permission.UPDATE_LESSON,
            Permission.UPDATE_LESSON_WITH_FILES,
            Permission.PUBLISH_LESSON,
            Permission.UPLOAD_LESSON_DOCUMENTS,

            // Assignment Management - Full Access
            Permission.CREATE_ASSIGNMENT,
            Permission.VIEW_ASSIGNMENT,
            Permission.UPDATE_ASSIGNMENT,
            Permission.UPDATE_ASSIGNMENT_WITH_FILES,
            Permission.PUBLISH_ASSIGNMENT,
            Permission.DELETE_ASSIGNMENT,

            // Submission Management - Grading Access
            Permission.VIEW_ASSIGNMENT_SUBMISSIONS,
            Permission.GRADE_SUBMISSION,

            // Department & Subject - Read Only
            Permission.VIEW_DEPARTMENTS,
            Permission.VIEW_DEPARTMENT_NAMES,
            Permission.VIEW_DEPARTMENT_NAME,
            Permission.VIEW_DEPARTMENT_TEACHERS,
            Permission.VIEW_SUBJECTS,
            Permission.VIEW_SUBJECT_COURSES,
            Permission.VIEW_SUBJECT_COURSES_PUBLIC,
            Permission.VIEW_MAJORS,
            Permission.VIEW_MAJOR_SUBJECTS,

            // Document & File Access
            Permission.ACCESS_STATIC_FILES,

            // Chatbot
            Permission.USE_CHATBOT,

            // Authentication & Health
            Permission.LOGIN,
            Permission.VIEW_ACCOUNT,
            Permission.REFRESH_TOKEN,
            Permission.LOGOUT,
            Permission.HEALTH_CHECK)),

    /**
     * STUDENT - Quyền sinh viên, chỉ có thể xem và nộp bài
     */
    STUDENT(Arrays.asList(// Course Access - Only enrolled courses
            Permission.VIEW_COURSES_BY_STUDENT,
            Permission.VIEW_STUDENT_COURSE_DETAILS,

            // Lesson Access - Read Only
            Permission.VIEW_LESSON,

            // Assignment Access - Read and Submit
            Permission.VIEW_ASSIGNMENT,
            Permission.SUBMIT_ASSIGNMENT,
            Permission.VIEW_UNSUBMITTED_COUNT,

            // Department & Subject - Read Only
            Permission.VIEW_DEPARTMENTS,
            Permission.VIEW_SUBJECT_COURSES_PUBLIC,

            // Document & File Access
            Permission.ACCESS_STATIC_FILES,

            // Chatbot
            Permission.USE_CHATBOT,

            // Authentication & Health
            Permission.LOGIN,
            Permission.VIEW_ACCOUNT,
            Permission.REFRESH_TOKEN,
            Permission.LOGOUT,
            Permission.HEALTH_CHECK));

    private final List<Permission> permissions;

    PermissionRole(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Lấy danh sách permissions cho một role cụ thể
     * 
     * @param roleName Tên role (Admin, Teacher, Student)
     * @return Danh sách permissions
     */
    public static List<Permission> getPermissionsForRole(String roleName) {
        if (roleName == null) {
            return Collections.emptyList();
        }

        // Chuyển đổi tên role từ database sang enum
        String enumRoleName = roleName.toUpperCase();
        if ("ADMIN".equals(enumRoleName)) {
            return ADMIN.getPermissions();
        } else if ("TEACHER".equals(enumRoleName)) {
            return TEACHER.getPermissions();
        } else if ("STUDENT".equals(enumRoleName)) {
            return STUDENT.getPermissions();
        }

        return Collections.emptyList();
    }

    /**
     * Kiểm tra xem role có quyền truy cập endpoint không
     * 
     * @param roleName Tên role
     * @param method   HTTP method
     * @param path     Đường dẫn endpoint
     * @return true nếu có quyền
     */
    public static boolean hasPermission(String roleName, org.springframework.http.HttpMethod method, String path) {
        List<Permission> permissions = getPermissionsForRole(roleName);
        return permissions.stream()
                .anyMatch(permission -> permission.matches(method, path));
    }

    /**
     * Lấy mô tả tất cả permissions cho một role
     * 
     * @param roleName Tên role
     * @return Danh sách mô tả permissions
     */
    public static List<String> getPermissionDescriptions(String roleName) {
        return getPermissionsForRole(roleName).stream()
                .map(Permission::getDescription)
                .toList();
    }
}
