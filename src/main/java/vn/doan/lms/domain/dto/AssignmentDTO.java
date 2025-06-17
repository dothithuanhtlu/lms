package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
