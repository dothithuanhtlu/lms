package vn.doan.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsById(long departmentId);

    boolean existsByDepartmentCode(String departmentCode);

    boolean existsByNameDepartment(String nameDepartment);

    Department findOneById(long id);
}
