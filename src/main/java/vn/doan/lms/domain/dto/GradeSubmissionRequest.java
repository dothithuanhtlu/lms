package vn.doan.lms.domain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeSubmissionRequest {

    @NotNull(message = "Submission ID mustn't be null")
    private Long submissionId;

    @DecimalMin(value = "0.0", message = "Score must be >= 0")
    @DecimalMax(value = "999.99", message = "Score must be <= 999.99")
    private Float score;

    private String feedback;
}
