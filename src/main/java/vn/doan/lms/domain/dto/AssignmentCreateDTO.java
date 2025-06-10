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
public class AssignmentCreateDTO {
    private String title;
    private String description;
    private String instructions;
    private String assignmentType; // HOMEWORK, QUIZ, EXAM, PROJECT, ESSAY
    private Float maxScore;
    private Integer timeLimitMinutes;
    private LocalDateTime dueDate;
    private Boolean isPublished;
    private Boolean allowLateSubmission;
    private Long courseId;
    private Long lessonId;
}
