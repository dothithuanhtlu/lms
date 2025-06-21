package vn.doan.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.doan.lms.domain.AssignmentComment;

import java.util.List;

@Repository
public interface AssignmentCommentRepository extends JpaRepository<AssignmentComment, Long> {

    List<AssignmentComment> findByAssignmentId(Long assignmentId);
}