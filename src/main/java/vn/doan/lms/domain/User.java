package vn.doan.lms.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.util.SecurityUtil;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(unique = true)
    @NotBlank(message = "UserCode mustn't be empty")
    private String userCode;

    @NotBlank(message = "Password mustn't be empty")
    private String password;

    @Column(unique = true)
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
    @NotNull(message = "DateOfBirth mustn't be empty")
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_room_id")
    private ClassRoom classRoom;

    @OneToOne(mappedBy = "headOfDepartment", fetch = FetchType.LAZY)
    private Department managedDepartmentAsHead;

    @OneToOne(mappedBy = "deputyHeadOfDepartment", fetch = FetchType.LAZY)
    private Department managedDepartmentAsDeputy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    @OneToMany(mappedBy = "advisor", fetch = FetchType.LAZY)
    private List<ClassRoom> advisingClasses = new ArrayList<>();

    public boolean isDepartmentHead() {
        return managedDepartmentAsHead != null;
    }

    public boolean isDepartmentDeputy() {
        return managedDepartmentAsDeputy != null;
    }

    public boolean isAdvisor() {
        return !advisingClasses.isEmpty();
    }

    // định dạng giờ ở FE, vì ở BE mặc định là GMT+0
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = Instant.now();
    }

    @Column(columnDefinition = "LONGTEXT")
    private String refreshToken;

}
