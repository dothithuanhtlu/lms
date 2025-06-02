package vn.doan.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Semester;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, String> {

}
