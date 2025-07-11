package vn.doan.lms.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAssignmentWithFilesRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    @Min(value = 0, message = "Max score must be at least 0")
    private Float maxScore;

    private String dueDate; // ISO format: 2025-07-01T23:59:59

    private Boolean allowLateSubmission;

    private Boolean isPublished;

    // JSON string chứa metadata của documents
    private String documentsMetadata;
}
