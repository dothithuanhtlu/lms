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

    @NotBlank(message = "Description mustn't be empty")
    private String description;

    public DepartmentDTO(Department department) {
        this.id = department.getId();
        this.departmentCode = department.getDepartmentCode();
        this.nameDepartment = department.getNameDepartment();
        this.description = department.getDescription();

    }

    public Department toDepartment() {
        Department department = new Department();
        department.setDepartmentCode(this.departmentCode);
        department.setNameDepartment(this.nameDepartment);
        department.setDescription(this.description);
        return department;
    }
}
