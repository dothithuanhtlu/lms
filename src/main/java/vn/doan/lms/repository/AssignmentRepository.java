package vn.doan.lms.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Assignment;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

        Page<Assignment> findByCourseId(Long courseId, Pageable pageable);

        @Query("SELECT a FROM Assignment a WHERE a.course.id = :courseId AND "
                        + "(:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
                        + "(:isPublished IS NULL OR a.isPublished = :isPublished) AND "
                        + "(:startDate IS NULL OR a.dueDate >= :startDate) AND "
                        + "(:endDate IS NULL OR a.dueDate <= :endDate) AND "
                        + "(:isLateSubmission IS NULL OR (a.isPublished = :isLateSubmission))")
        Page<Assignment> filterAssignments(Long courseId, String keyword, Boolean isPublished, LocalDate startDate,
                        LocalDate endDate, Boolean isLateSubmission, Pageable pageable);
        // List<Assignment> findByCourseIdAndIsPublishedTrue(Long courseId);

        // List<Assignment> findByLessonId(Long lessonId);

        // List<Assignment> findByCourseIdAndAssignmentType(Long courseId,
        // AssignmentType assignmentType);

        // @Query("SELECT a FROM Assignment a WHERE a.course.id = :courseId AND
        // a.dueDate BETWEEN :startDate AND :endDate")
        // List<Assignment> findByCourseIdAndDueDateBetween(@Param("courseId") Long
        // courseId,
        // @Param("startDate") LocalDateTime startDate,
        // @Param("endDate") LocalDateTime endDate);

        // @Query("SELECT a FROM Assignment a WHERE a.dueDate < :currentTime AND
        // a.isPublished = true")
        // List<Assignment> findOverdueAssignments(@Param("currentTime") LocalDateTime
        // currentTime);
        long countByCourseIdAndIsPublishedTrue(long courseId);

        long countByCourseId(Long courseId);

        long countByCourseIdAndIsPublished(Long courseId, Boolean isPublished);
}
