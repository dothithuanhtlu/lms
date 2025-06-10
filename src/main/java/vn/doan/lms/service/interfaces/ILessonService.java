package vn.doan.lms.service.interfaces;

import java.util.List;

import vn.doan.lms.domain.dto.LessonCreateDTO;
import vn.doan.lms.domain.dto.LessonDTO;

public interface ILessonService {

    List<LessonDTO> getLessonsByCourseId(Long courseId);

    LessonDTO createLesson(LessonCreateDTO createDTO);

    LessonDTO updateLesson(Long lessonId, LessonCreateDTO updateDTO);

    void deleteLesson(Long lessonId);

    LessonDTO getLessonById(Long lessonId);

    void publishLesson(Long lessonId);

    void unpublishLesson(Long lessonId);

    void reorderLessons(Long courseId, List<Long> lessonIds);

    List<LessonDTO> getPublishedLessonsByCourseId(Long courseId);

    long countLessonsByCourse(Long courseId);
}
