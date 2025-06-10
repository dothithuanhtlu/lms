package vn.doan.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.LessonDocument;
import vn.doan.lms.domain.LessonDocument.DocumentType;

@Repository
public interface LessonDocumentRepository extends JpaRepository<LessonDocument, Long> {

    List<LessonDocument> findByLessonId(Long lessonId);

    List<LessonDocument> findByLessonIdAndDocumentType(Long lessonId, DocumentType documentType);

    @Query("SELECT ld FROM LessonDocument ld WHERE ld.lesson.course.id = :courseId")
    List<LessonDocument> findByCourseId(@Param("courseId") Long courseId);

    long countByLessonId(Long lessonId);

    boolean existsByLessonIdAndFileName(Long lessonId, String fileName);
}
