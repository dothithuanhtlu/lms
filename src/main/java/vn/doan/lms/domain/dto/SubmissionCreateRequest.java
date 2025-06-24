package vn.doan.lms.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionCreateRequest {
    @NotNull(message = "Assignment ID mustn't be null")
    private Long assignmentId;

    // JSON string chứa metadata của files nộp bài
    private String documentsMetadata;
}
