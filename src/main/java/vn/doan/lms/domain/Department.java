package vn.doan.lms.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "DepartmentCode mustn't be empty")
    private String departmentCode;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "NameDepartment mustn't be empty")
    private String nameDepartment;

    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_of_department_id", unique = true)
    private User headOfDepartment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deputy_head_of_department_id", unique = true)
    private User deputyHeadOfDepartment;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    public boolean isUserHeadOrDeputy(User user) {
        return user.equals(headOfDepartment) || user.equals(deputyHeadOfDepartment);
    }

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Major> majors = new ArrayList<>();
}
