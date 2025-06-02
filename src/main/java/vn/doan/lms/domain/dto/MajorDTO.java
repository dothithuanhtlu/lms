package vn.doan.lms.domain.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.Major;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MajorDTO {
    private long id;
    @NotBlank(message = "MajorCode mustn't be empty")
    private String majorCode;

    @Column(nullable = false)
    @NotBlank(message = "MajorName mustn't be empty")
    private String majorName;

    private String description;

    public MajorDTO(Major major) {
        this.id = major.getId();
        this.majorCode = major.getMajorCode();
        this.majorName = major.getMajorName();
        this.description = major.getDescription();
    }

}
