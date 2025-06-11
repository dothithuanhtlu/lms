package vn.doan.lms.domain.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.doan.lms.domain.Course;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseFullDetailDTO {
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxStudents;
    private Integer currentStudents;
    private String status;
    // Subject information
    private SubjectInfo subject;

    // Lists
    private List<StudentInfo> students;
    private List<LessonInfo> lessons;
    private List<AssignmentInfo> assignments;

    public CourseFullDetailDTO(Course course) {
        this.id = course.getId();
        this.courseCode = course.getCourseCode();
        this.courseName = course.getSubject() != null ? course.getSubject().getSubjectName() : null;
        this.description = course.getSubject() != null ? course.getSubject().getDescription() : null;
        this.startDate = course.getStartDate();
        this.endDate = course.getEndDate();
        this.maxStudents = course.getMaxStudents();
        this.currentStudents = course.getEnrollments() != null ? course.getEnrollments().size() : 0;

        // Calculate status
        if (course.getEndDate() != null && course.getEndDate().isBefore(LocalDate.now())) {
            this.status = "Completed";
        } else if (course.getStartDate() != null && course.getStartDate().isAfter(LocalDate.now())) {
            this.status = "Upcoming";
        } else {
            this.status = "Active";
        }
        // Subject info
        if (course.getSubject() != null) {
            this.subject = SubjectInfo.builder()
                    .id(course.getSubject().getId())
                    .subjectCode(course.getSubject().getSubjectCode())
                    .subjectName(course.getSubject().getSubjectName())
                    .credits(course.getSubject().getCredits())
                    .description(course.getSubject().getDescription())
                    .build();
        }

        // Students info
        if (course.getEnrollments() != null) {
            this.students = course.getEnrollments().stream()
                    .map(enrollment -> StudentInfo.builder()
                            .id(enrollment.getStudent().getId())
                            .userCode(enrollment.getStudent().getUserCode())
                            .fullName(enrollment.getStudent().getFullName())
                            .email(enrollment.getStudent().getEmail())
                            .className(enrollment.getStudent().getClassRoom() != null
                                    ? enrollment.getStudent().getClassRoom().getClassName()
                                    : null)
                            .enrollmentStatus(enrollment.getStatus())
                            .midtermScore(enrollment.getMidtermScore())
                            .finalScore(enrollment.getFinalScore())
                            .build())
                    .collect(Collectors.toList());
        }

        // Lessons info
        if (course.getLessons() != null) {
            this.lessons = course.getLessons().stream()
                    .map(lesson -> LessonInfo.builder()
                            .id(lesson.getId())
                            .title(lesson.getTitle())
                            .description(lesson.getDescription())
                            .lessonOrder(lesson.getLessonOrder())
                            .durationMinutes(lesson.getDurationMinutes())
                            .isPublished(lesson.getIsPublished())
                            .totalDocuments(0) // Will be populated separately to avoid MultipleBagFetchException
                            .build())
                    .collect(Collectors.toList());
        }

        // Assignments info
        if (course.getAssignments() != null) {
            this.assignments = course.getAssignments().stream()
                    .map(assignment -> AssignmentInfo.builder()
                            .id(assignment.getId())
                            .title(assignment.getTitle())
                            .description(assignment.getDescription()).type(assignment.getAssignmentType().name())
                            .maxScore(assignment.getMaxScore())
                            .timeLimit(assignment.getTimeLimitMinutes())
                            .dueDate(assignment.getDueDate())
                            .isPublished(assignment.getIsPublished()).totalQuestions(0) // Will be populated separately
                                                                                        // to avoid
                                                                                        // MultipleBagFetchException
                            .totalSubmissions(0) // Will be populated separately to avoid MultipleBagFetchException
                            .build())
                    .collect(Collectors.toList());
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectInfo {
        private Long id;
        private String subjectCode;
        private String subjectName;
        private Integer credits;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String userCode;
        private String fullName;
        private String email;
        private String className;
        private String enrollmentStatus;
        private Float midtermScore;
        private Float finalScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonInfo {
        private Long id;
        private String title;
        private String description;
        private Integer lessonOrder;
        private Integer durationMinutes;
        private Boolean isPublished;
        private Integer totalDocuments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentInfo {
        private Long id;
        private String title;
        private String description;
        private String type;
        private Float maxScore;
        private Integer timeLimit;
        private java.time.LocalDateTime dueDate;
        private Boolean isPublished;
        private Integer totalQuestions;
        private Integer totalSubmissions;
    }
}
