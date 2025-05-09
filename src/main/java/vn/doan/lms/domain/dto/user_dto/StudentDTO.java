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
import vn.doan.lms.util.SecurityUtil;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {

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

    @NotBlank(message = "Role mustn't be empty")
    private String roleName;

    @NotBlank(message = "ClassRoom mustn't be empty")
    private String className;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
