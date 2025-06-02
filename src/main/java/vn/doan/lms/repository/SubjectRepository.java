package vn.doan.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.doan.lms.domain.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findAllByMajorId(Long majorId);
}
