package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCreateDTO {

    @NotBlank(message = "Assignment title is required")
    private String title;

    private String description;

    @Positive(message = "Max score must be positive")
    private Float maxScore;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @Builder.Default
    private Boolean isPublished = false;

    @Builder.Default
    private Boolean allowLateSubmission = false;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    // File metadata for uploaded files
    private String[] fileDescriptions;
    private Integer[] fileDisplayOrders;
}
