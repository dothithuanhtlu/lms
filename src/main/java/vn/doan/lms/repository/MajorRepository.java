package vn.doan.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.doan.lms.domain.Major;

public interface MajorRepository extends JpaRepository<Major, Long> {
    List<Major> findAllByDepartmentId(Long departmentId);

    List<Major> findAllByDepartmentNameDepartment(String departmentName);
}
