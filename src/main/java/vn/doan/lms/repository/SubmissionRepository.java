package vn.doan.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Submission;
import vn.doan.lms.domain.Submission.SubmissionStatus;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByAssignmentId(Long assignmentId);

    List<Submission> findByStudentId(Long studentId);

    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    List<Submission> findByAssignmentIdAndStatus(Long assignmentId, SubmissionStatus status);

    @Query("SELECT s FROM Submission s WHERE s.assignment.course.id = :courseId AND s.student.id = :studentId")
    List<Submission> findByCourseIdAndStudentId(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    @Query("SELECT s FROM Submission s WHERE s.assignment.course.id = :courseId")
    List<Submission> findByCourseId(@Param("courseId") Long courseId);

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    long countByAssignmentId(Long assignmentId);

    long countByAssignmentIdAndStatus(Long assignmentId, SubmissionStatus status);

    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.score IS NOT NULL")
    Double getAverageScoreByAssignmentId(@Param("assignmentId") Long assignmentId);
}
