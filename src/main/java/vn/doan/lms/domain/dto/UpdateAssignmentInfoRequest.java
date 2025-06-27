package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAssignmentInfoRequest {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotNull(message = "Due date must not be null")
    private LocalDateTime dueDate;

    private String description;

    @Min(value = 0, message = "Max score must be at least 0")
    private Float maxScore;

    private Boolean isPublished;

    private Boolean allowLateSubmission;
}
