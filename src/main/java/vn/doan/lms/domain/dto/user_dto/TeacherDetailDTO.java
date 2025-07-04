package vn.doan.lms.domain.dto.user_dto;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.Department;
import vn.doan.lms.domain.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDetailDTO {
    @NotBlank(message = "UserCode mustn't be empty")
    private String userCode;

    @Email(message = "Email is invalid")
    @NotBlank(message = "Email mustn't be empty")
    private String email;

    @NotBlank(message = "FullName mustn't be empty")
    private String fullName;

    @NotBlank(message = "Address mustn't be empty")
    private String address;

    @NotBlank(message = "Gender mustn't be empty")
    @Pattern(regexp = "MALE|FEMALE", message = "Gender must be either MALE or FEMALE")
    private String gender;

    @NotBlank(message = "Phone mustn't be empty")
    private String phone;

    @NotBlank(message = "DateOfBirth mustn't be empty")
    private LocalDate dateOfBirth;

    private String departmentName;
    private String departmentCode;
    private boolean isAdvisor;

    public TeacherDetailDTO(User user) {
        this.userCode = user.getUserCode();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.address = user.getAddress();
        this.gender = user.getGender();
        this.phone = user.getPhone();
        this.dateOfBirth = user.getDateOfBirth();
        departmentName = user.getDepartment() != null ? user.getDepartment().getNameDepartment() : null;
        departmentCode = user.getDepartment() != null ? user.getDepartment().getDepartmentCode() : null;

        // Kiểm tra giáo viên chủ nhiệm
        this.isAdvisor = user.getClassRoom() != null
                && user.getClassRoom().getAdvisor() != null
                && user.getClassRoom().getAdvisor().equals(user);
    }

}
