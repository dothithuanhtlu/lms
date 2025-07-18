package vn.doan.lms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.doan.lms.domain.Quiz;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Page<Quiz> findAllByCourseId(Long courseId, Pageable pageable);

    @Query("SELECT q FROM Quiz q WHERE q.course.id = :courseId AND q.isActive = true ANd q.endTime >= CURRENT_TIMESTAMP")
    Page<Quiz> studentGetByCourseId(Long courseId, Pageable pageable);
}
