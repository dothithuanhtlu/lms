package vn.doan.lms.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Assignment;
import vn.doan.lms.domain.Assignment.AssignmentType;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCourseId(Long courseId);

    List<Assignment> findByCourseIdAndIsPublishedTrue(Long courseId);

    List<Assignment> findByLessonId(Long lessonId);

    List<Assignment> findByCourseIdAndAssignmentType(Long courseId, AssignmentType assignmentType);

    @Query("SELECT a FROM Assignment a WHERE a.course.id = :courseId AND a.dueDate BETWEEN :startDate AND :endDate")
    List<Assignment> findByCourseIdAndDueDateBetween(@Param("courseId") Long courseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Assignment a WHERE a.dueDate < :currentTime AND a.isPublished = true")
    List<Assignment> findOverdueAssignments(@Param("currentTime") LocalDateTime currentTime);

    long countByCourseId(Long courseId);

    long countByCourseIdAndAssignmentType(Long courseId, AssignmentType assignmentType);

    long countByCourseIdAndIsPublishedTrue(Long courseId);
}
