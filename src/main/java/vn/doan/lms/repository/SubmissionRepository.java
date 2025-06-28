package vn.doan.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Submission;
import vn.doan.lms.domain.Submission.SubmissionStatus;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

        // Basic find methods
        List<Submission> findByAssignmentId(Long assignmentId);

        // Pageable versions
        Page<Submission> findByAssignmentId(Long assignmentId, Pageable pageable);

        List<Submission> findByStudentId(Long studentId);

        // Pageable version
        Page<Submission> findByStudentId(Long studentId, Pageable pageable);

        Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

        List<Submission> findByAssignmentIdAndStatus(Long assignmentId, SubmissionStatus status);

        @Query("SELECT s FROM Submission s WHERE s.assignment.course.id = :courseId AND s.student.id = :studentId")
        List<Submission> findByCourseIdAndStudentId(@Param("courseId") Long courseId,
                        @Param("studentId") Long studentId);

        @Query("SELECT s FROM Submission s WHERE s.assignment.course.id = :courseId")
        List<Submission> findByCourseId(@Param("courseId") Long courseId);

        // Pageable version for course submissions
        @Query("SELECT s FROM Submission s WHERE s.assignment.course.id = :courseId")
        Page<Submission> findByCourseId(@Param("courseId") Long courseId, Pageable pageable);

        // Combined query for student submissions by course
        @Query("SELECT s FROM Submission s WHERE s.student.id = :studentId AND s.assignment.course.id = :courseId")
        Page<Submission> findByStudentIdAndAssignmentCourseId(@Param("studentId") Long studentId,
                        @Param("courseId") Long courseId, Pageable pageable);

        @Query("SELECT s FROM Submission s WHERE s.student.id = :studentId AND s.assignment.course.id = :courseId")
        List<Submission> findByStudentIdAndAssignmentCourseId(@Param("studentId") Long studentId,
                        @Param("courseId") Long courseId);

        boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

        long countByAssignmentId(Long assignmentId);

        long countByAssignmentIdAndStatus(Long assignmentId, SubmissionStatus status);

        @Query("SELECT AVG(s.score) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.score IS NOT NULL")
        Double getAverageScoreByAssignmentId(@Param("assignmentId") Long assignmentId);

        /**
         * Count assignments that a student hasn't submitted yet
         * Only counts published assignments from courses the student is enrolled in
         */
        @Query("SELECT COUNT(a) FROM Assignment a " +
                        "JOIN a.course c " +
                        "JOIN c.enrollments e " +
                        "WHERE e.student.id = :studentId " +
                        "AND a.isPublished = true " +
                        "AND NOT EXISTS (SELECT s FROM Submission s WHERE s.assignment.id = a.id AND s.student.id = :studentId)")
        long countUnsubmittedAssignmentsByStudentId(@Param("studentId") Long studentId);
}
