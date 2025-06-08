package vn.doan.lms.service.implements_class;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Major;
import vn.doan.lms.domain.dto.MajorDTO;
import vn.doan.lms.domain.dto.MajorRequestDTO;
import vn.doan.lms.repository.DepartmentRepository;
import vn.doan.lms.repository.MajorRepository;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class MajorService {
    private final MajorRepository majorRepository;
    private final DepartmentRepository departmentRepository;

    private boolean isExistsMajorId(long majorId) {
        if (!majorRepository.existsById(majorId)) {
            throw new ResourceNotFoundException("Major not found with id: " + majorId);
        }
        return true;
    }

    public String getMajorNameByMajorId(long majorId) {
        isExistsMajorId(majorId);
        return this.majorRepository.findById(majorId).get().getMajorName();
    }

    public List<MajorDTO> getMajorDTOsByDepartmentId(long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }
        List<Major> majors = this.majorRepository.findAllByDepartmentId(departmentId);
        return majors.stream()
                .map(MajorDTO::new)
                .collect(Collectors.toList());
    }

    public List<MajorDTO> getAllMajors() {
        return majorRepository.findAll().stream()
                .map(MajorDTO::new)
                .collect(Collectors.toList());
    }

    public List<MajorRequestDTO> getMajorRequestDTOsByDepartmentId(long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }
        return majorRepository.findAllByDepartmentId(departmentId).stream()
                .map(major -> MajorRequestDTO.builder()
                        .id(major.getId())
                        .majorName(major.getMajorName())
                        .build())
                .collect(Collectors.toList());
    }

    public List<MajorDTO> getMajorsByDepartmentName(String departmentName) {
        if (!departmentRepository.existsByNameDepartment(departmentName)) {
            throw new ResourceNotFoundException("Department not found with name: " + departmentName);
        }
        return majorRepository.findAllByDepartmentNameDepartment(departmentName).stream()
                .map(MajorDTO::new)
                .collect(Collectors.toList());
    }
}
