package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.doan.lms.domain.Submission.SubmissionStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {
    private Long id;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private Float score;
    private String feedback;
    private SubmissionStatus status;
    private Boolean isLate;

    // Assignment info
    private Long assignmentId;
    private String assignmentTitle;
    private Float assignmentMaxScore;

    // Student info
    private Long studentId;
    private String studentName;
    private String studentEmail;

    // Grader info
    private Long gradedById;
    private String gradedByName;

    // Files
    private List<SubmissionDocumentResponse> documents;
}
