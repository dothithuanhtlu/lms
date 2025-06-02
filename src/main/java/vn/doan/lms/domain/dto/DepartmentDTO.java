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

    private Long headOfDepartmentId; // ID trưởng khoa
    private Long deputyHeadOfDepartmentId; // ID phó khoa

    @NotBlank(message = "Description mustn't be empty")
    private String description;

    // Constructor chuyển từ Entity sang DTO
    public DepartmentDTO(Department department) {
        this.id = department.getId();
        this.departmentCode = department.getDepartmentCode();
        this.nameDepartment = department.getNameDepartment();
        this.description = department.getDescription();

        this.headOfDepartmentId = department.getHeadOfDepartment() != null
                ? department.getHeadOfDepartment().getId()
                : null;

        this.deputyHeadOfDepartmentId = department.getDeputyHeadOfDepartment() != null
                ? department.getDeputyHeadOfDepartment().getId()
                : null;
    }

    public Department toDepartment() {
        Department department = new Department();
        department.setDepartmentCode(this.departmentCode);
        department.setNameDepartment(this.nameDepartment);
        department.setDescription(this.description);
        return department;
    }
}
