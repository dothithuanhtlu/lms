package vn.doan.lms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.Department;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentDTO {
    private long id;

    @NotBlank(message = "DepartmentCode mustn't be empty")
    private String departmentCode;

    @NotBlank(message = "NameDepartment mustn't be empty")
    private String nameDepartment;

    private Long headOfDepartmentId;
    private String headOfDepartmentName;
    private String headOfDepartmentCode;

    private Long deputyHeadOfDepartmentId;
    private String deputyHeadOfDepartmentName;
    private String deputyHeadOfDepartmentCode;

    @NotBlank(message = "Description mustn't be empty")
    private String description;

    public DepartmentDTO(Department department) {
        this.id = department.getId();
        this.departmentCode = department.getDepartmentCode();
        this.nameDepartment = department.getNameDepartment();
        this.description = department.getDescription();

        if (department.getHeadOfDepartment() != null) {
            this.headOfDepartmentId = department.getHeadOfDepartment().getId();
            this.headOfDepartmentName = department.getHeadOfDepartment().getFullName();
            this.headOfDepartmentCode = department.getHeadOfDepartment().getUserCode();
        }

        if (department.getDeputyHeadOfDepartment() != null) {
            this.deputyHeadOfDepartmentId = department.getDeputyHeadOfDepartment().getId();
            this.deputyHeadOfDepartmentName = department.getDeputyHeadOfDepartment().getFullName();
            this.deputyHeadOfDepartmentCode = department.getDeputyHeadOfDepartment().getUserCode();
        }
    }

    public Department toDepartment() {
        Department department = new Department();
        department.setDepartmentCode(this.departmentCode);
        department.setNameDepartment(this.nameDepartment);
        department.setDescription(this.description);
        return department;
    }
}
