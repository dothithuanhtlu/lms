package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.Submission;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDTO {
    private Long id;
    private String content;
    private String filePath;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private Float score;
    private String feedback;
    private String status; // SUBMITTED, GRADED, RETURNED, LATE
    private Boolean isLate;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentCode;
    private String studentName;
    private String gradedByName;

    public SubmissionDTO(Submission submission) {
        this.id = submission.getId();
        this.content = submission.getContent();
        this.filePath = submission.getFilePath();
        this.submittedAt = submission.getSubmittedAt();
        this.gradedAt = submission.getGradedAt();
        this.score = submission.getScore();
        this.feedback = submission.getFeedback();
        this.status = submission.getStatus().name();
        this.isLate = submission.getIsLate();
        this.assignmentId = submission.getAssignment().getId();
        this.assignmentTitle = submission.getAssignment().getTitle();
        this.studentId = submission.getStudent().getId();
        this.studentCode = submission.getStudent().getUserCode();
        this.studentName = submission.getStudent().getFullName();

        if (submission.getGradedBy() != null) {
            this.gradedByName = submission.getGradedBy().getFullName();
        }
    }
}
