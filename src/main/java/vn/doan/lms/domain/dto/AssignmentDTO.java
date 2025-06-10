package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.Assignment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
    private Long id;
    private String title;
    private String description;
    private String instructions;
    private String assignmentType; // HOMEWORK, QUIZ, EXAM, PROJECT, ESSAY
    private Float maxScore;
    private Integer timeLimitMinutes;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublished;
    private Boolean allowLateSubmission;
    private Long courseId;
    private String courseCode;
    private Long lessonId;
    private String lessonTitle;
    private String createdByName;
    private Integer totalQuestions;
    private Integer totalSubmissions;

    public AssignmentDTO(Assignment assignment) {
        this.id = assignment.getId();
        this.title = assignment.getTitle();
        this.description = assignment.getDescription();
        this.instructions = assignment.getInstructions();
        this.assignmentType = assignment.getAssignmentType().name();
        this.maxScore = assignment.getMaxScore();
        this.timeLimitMinutes = assignment.getTimeLimitMinutes();
        this.dueDate = assignment.getDueDate();
        this.createdAt = assignment.getCreatedAt();
        this.updatedAt = assignment.getUpdatedAt();
        this.isPublished = assignment.getIsPublished();
        this.allowLateSubmission = assignment.getAllowLateSubmission();
        this.courseId = assignment.getCourse().getId();
        this.courseCode = assignment.getCourse().getCourseCode();

        if (assignment.getLesson() != null) {
            this.lessonId = assignment.getLesson().getId();
            this.lessonTitle = assignment.getLesson().getTitle();
        }

        if (assignment.getCreatedBy() != null) {
            this.createdByName = assignment.getCreatedBy().getFullName();
        }

        this.totalQuestions = assignment.getQuestions() != null ? assignment.getQuestions().size() : 0;
        this.totalSubmissions = assignment.getSubmissions() != null ? assignment.getSubmissions().size() : 0;
    }
}
