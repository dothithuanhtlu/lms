package vn.doan.lms.service.implements_class;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Department;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.DepartmentDTO;
import vn.doan.lms.domain.dto.user_dto.TeacherDTO;
import vn.doan.lms.repository.DepartmentRepository;
import vn.doan.lms.repository.MajorRepository;
import vn.doan.lms.repository.SubjectRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private static final String ROLE_TEACHER = "TEACHER";
    private final SubjectRepository subjectRepository;

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentDTO::new) // Convert từng Department -> DepartmentDTO
                .collect(Collectors.toList());
    }

    private boolean isExistDepartmentCode(String departmentCode) {
        return departmentRepository.existsByDepartmentCode(departmentCode);
    }

    private boolean isExistDepartmentName(String departmentName) {
        return departmentRepository.existsByNameDepartment(departmentName);
    }

    public Department createDepartment(DepartmentDTO departmentDTO) {
        if (isExistDepartmentCode(departmentDTO.getDepartmentCode())
                || isExistDepartmentName(departmentDTO.getNameDepartment())) {
            throw new BadRequestExceptionCustom("Department code or name already exists");
        }
        Department department = departmentDTO.toDepartment();
        return this.departmentRepository.save(department);
    }

    public List<TeacherDTO> getAllTeacherByDepartmentId(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }

        // Lấy toàn bộ giáo viên thuộc khoa
        List<User> teachers = userRepository.findByDepartmentIdAndRole_NameRole(
                departmentId,
                ROLE_TEACHER);

        return teachers.stream()
                .map(TeacherDTO::new)
                .collect(Collectors.toList());
    }

    public String getDepartmentNameByDepartmentId(long departmentId) {
        if (!this.departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }
        return this.departmentRepository.findOneById(departmentId).getNameDepartment();
    }

    public List<String> getAllDepartmentNames() {
        return this.departmentRepository.findAll().stream()
                .map(Department::getNameDepartment)
                .collect(Collectors.toList());
    }
}
