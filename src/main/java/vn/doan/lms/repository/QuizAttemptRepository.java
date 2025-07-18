package vn.doan.lms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.doan.lms.domain.Quiz;
import vn.doan.lms.domain.QuizAttempt;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Page<QuizAttempt> findAllByQuizId(Long quizId, Pageable pageable);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.course.id = :courseId AND qa.student.userCode = :userCode")
    Page<QuizAttempt> getQuizAttemptByUserAndCourseId(Long courseId, String userCode, Pageable pageable);

    Long quiz(Quiz quiz);
}
