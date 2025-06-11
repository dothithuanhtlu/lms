package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.doan.lms.domain.Enrollment;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class EnrollmentDTO {
    private long id;
    private Long studentId;
    private String studentCode;
    private String studentName;
    private LocalDateTime enrolledAt = LocalDateTime.now();
    private Float midtermScore;
    private Float finalScore;
    @Pattern(regexp = "REGISTERED|COMPLETED|DROPPED|FAILED", message = "Status mustn't be empty")
    private String status;

    public EnrollmentDTO(Enrollment enrollment) {
        this.id = enrollment.getId();
        this.studentId = enrollment.getStudent().getId();
        this.studentCode = enrollment.getStudent().getUserCode();
        this.studentName = enrollment.getStudent().getFullName();
        this.midtermScore = enrollment.getMidtermScore();
        this.finalScore = enrollment.getFinalScore();
        this.status = enrollment.getStatus();
    }
}
