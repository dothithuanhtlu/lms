package vn.doan.lms.domain.dto;

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
public class CreateLessonWithFilesRequest {

    @NotBlank(message = "Lesson title mustn't be empty")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Course ID mustn't be null")
    private Long courseId;

    @Builder.Default
    private Boolean isPublished = false;

    // JSON string chứa metadata của documents
    private String documentsMetadata;
}
