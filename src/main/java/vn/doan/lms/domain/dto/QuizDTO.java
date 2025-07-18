package vn.doan.lms.domain.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.doan.lms.domain.Quiz;

import java.time.LocalDateTime;

@Data
public class QuizDTO {

    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Quiz title mustn't be empty")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Course mustn't be null")
    private Long courseId;

    @NotNull(message = "Duration mustn't be null")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Start time mustn't be null")
    private LocalDateTime startTime;

    @NotNull(message = "End time mustn't be null")
    private LocalDateTime endTime;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuizDTO mapToDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setCourseId(quiz.getCourse().getId());
        dto.setDurationMinutes(quiz.getDurationMinutes());
        dto.setStartTime(quiz.getStartTime());
        dto.setEndTime(quiz.getEndTime());
        dto.setIsActive(quiz.getIsActive());
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setUpdatedAt(quiz.getUpdatedAt());
        return dto;
    }
}
