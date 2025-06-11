package vn.doan.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseIdOrderByLessonOrderAsc(Long courseId);

    List<Lesson> findByCourseIdAndIsPublishedTrueOrderByLessonOrderAsc(Long courseId);

    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId AND l.isPublished = true ORDER BY l.lessonOrder ASC")
    List<Lesson> findPublishedLessonsByCourseId(@Param("courseId") Long courseId);

    boolean existsByCourseIdAndLessonOrder(Long courseId, Integer lessonOrder);

    @Query("SELECT MAX(l.lessonOrder) FROM Lesson l WHERE l.course.id = :courseId")
    Integer findMaxLessonOrderByCourseId(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);

    long countByCourseIdAndIsPublishedTrue(Long courseId);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.documents WHERE l.id = :lessonId")
    Optional<Lesson> findByIdWithDocuments(@Param("lessonId") Long lessonId);
}
