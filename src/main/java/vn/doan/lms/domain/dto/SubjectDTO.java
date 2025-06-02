package vn.doan.lms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.doan.lms.domain.Subject;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SubjectDTO {
    private long id;

    @NotBlank(message = "SubjectCode mustn't be empty")
    private String subjectCode; // VD: "CS101"

    @NotBlank(message = "SubjectName mustn't be empty")
    private String subjectName;

    @NotBlank(message = "Credits mustn't be empty")
    private Integer credits;

    @NotBlank(message = "SubjectType mustn't be empty")
    @Pattern(regexp = "GENERAL|SPECIALIZED", message = "SubjectType must be GENERAL or SPECIALIZED")
    private String type;

    public SubjectDTO(Subject subject) {
        this.id = subject.getId();
        this.subjectCode = subject.getSubjectCode();
        this.subjectName = subject.getSubjectName();
        this.credits = subject.getCredits();
        this.type = subject.getType();

    }
}
