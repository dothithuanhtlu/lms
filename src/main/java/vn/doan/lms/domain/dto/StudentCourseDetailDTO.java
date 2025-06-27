package vn.doan.lms.domain.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for student course details
 * Contains course information and student's assignment submission status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCourseDetailDTO {

    // Course basic info
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxStudents;
    private Integer currentStudents;
    private String status;

    // Subject info
    private SubjectDTO subject;

    // Teacher info
    private TeacherInfo teacher;

    // Student's personal info
    private StudentPersonalInfo studentInfo;

    // Assignments with student's submission status
    private List<StudentAssignmentInfo> assignments;

    // Course statistics for the student
    private StudentCourseStatistics statistics;

    /**
     * Teacher information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeacherInfo {
        private Long id;
        private String userCode;
        private String fullName;
        private String email;
    }

    /**
     * Student's personal information in this course
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentPersonalInfo {
        private Long id;
        private String userCode;
        private String fullName;
        private String email;
        private String className;
        private String enrollmentStatus;
        private Float midtermScore;
        private Float finalScore;
    }

    /**
     * Assignment information with student's submission status
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentAssignmentInfo {
        private Long id;
        private String title;
        private String description;
        private Float maxScore;
        private String dueDate;
        private Boolean isPublished;
        private Boolean allowLateSubmission;

        // Student's submission info for this assignment
        private StudentSubmissionInfo submission;
    }

    /**
     * Student's submission information for an assignment
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentSubmissionInfo {
        private Long submissionId;
        private Boolean hasSubmitted;
        private String submissionDate;
        private Float score;
        private String feedback;
        private String status; // NOT_SUBMITTED, SUBMITTED, LATE
        private Boolean isLate;
        private Boolean canEdit; // Can student edit this submission
        private Boolean canDelete; // Can student delete this submission
        private Integer documentCount; // Number of files submitted
    }

    /**
     * Course statistics for the student
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentCourseStatistics {
        private Integer totalAssignments;
        private Integer submittedAssignments;
        private Integer gradedAssignments;
        private Integer lateSubmissions;
        private Float averageScore; // Average of graded assignments
        private Double completionRate; // Percentage of assignments submitted
    }
}
