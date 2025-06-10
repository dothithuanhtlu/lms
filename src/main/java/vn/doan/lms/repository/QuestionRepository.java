package vn.doan.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Question;
import vn.doan.lms.domain.Question.QuestionType;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByAssignmentIdOrderByQuestionOrderAsc(Long assignmentId);

    List<Question> findByAssignmentIdAndQuestionType(Long assignmentId, QuestionType questionType);

    @Query("SELECT q FROM Question q WHERE q.assignment.course.id = :courseId")
    List<Question> findByCourseId(@Param("courseId") Long courseId);

    long countByAssignmentId(Long assignmentId);

    @Query("SELECT SUM(q.points) FROM Question q WHERE q.assignment.id = :assignmentId")
    Float getTotalPointsByAssignmentId(@Param("assignmentId") Long assignmentId);

    boolean existsByAssignmentIdAndQuestionOrder(Long assignmentId, Integer questionOrder);

    @Query("SELECT MAX(q.questionOrder) FROM Question q WHERE q.assignment.id = :assignmentId")
    Integer findMaxQuestionOrderByAssignmentId(@Param("assignmentId") Long assignmentId);
}
