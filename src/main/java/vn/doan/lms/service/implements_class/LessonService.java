package vn.doan.lms.service.implements_class;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.Lesson;
import vn.doan.lms.domain.dto.LessonCreateDTO;
import vn.doan.lms.domain.dto.LessonDTO;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.LessonRepository;
import vn.doan.lms.service.interfaces.ILessonService;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
@Transactional
public class LessonService implements ILessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Override
    public List<LessonDTO> getLessonsByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return lessonRepository.findByCourseIdOrderByLessonOrderAsc(courseId)
                .stream()
                .map(LessonDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public LessonDTO createLesson(LessonCreateDTO createDTO) {
        Course course = courseRepository.findById(createDTO.getCourseId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Course not found with id: " + createDTO.getCourseId()));

        // Get next order index
        Integer maxOrder = lessonRepository.findMaxLessonOrderByCourseId(createDTO.getCourseId());
        int nextOrder = (maxOrder != null) ? maxOrder + 1 : 1;

        Lesson lesson = Lesson.builder()
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .content(createDTO.getContent())
                .course(course)
                .lessonOrder(nextOrder)
                .durationMinutes(createDTO.getDurationMinutes())
                .isPublished(createDTO.getIsPublished() != null ? createDTO.getIsPublished() : false)
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);
        return new LessonDTO(savedLesson);
    }

    @Override
    public LessonDTO updateLesson(Long lessonId, LessonCreateDTO updateDTO) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        lesson.setTitle(updateDTO.getTitle());
        lesson.setDescription(updateDTO.getDescription());
        lesson.setContent(updateDTO.getContent());
        lesson.setDurationMinutes(updateDTO.getDurationMinutes());

        if (updateDTO.getIsPublished() != null) {
            lesson.setIsPublished(updateDTO.getIsPublished());
        }

        Lesson savedLesson = lessonRepository.save(lesson);
        return new LessonDTO(savedLesson);
    }

    @Override
    public void deleteLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }
        lessonRepository.deleteById(lessonId);
    }

    @Override
    public LessonDTO getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
        return new LessonDTO(lesson);
    }

    @Override
    public void publishLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
        lesson.setIsPublished(true);
        lessonRepository.save(lesson);
    }

    @Override
    public void unpublishLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
        lesson.setIsPublished(false);
        lessonRepository.save(lesson);
    }

    @Override
    public void reorderLessons(Long courseId, List<Long> lessonIds) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        for (int i = 0; i < lessonIds.size(); i++) {
            Long lessonId = lessonIds.get(i);
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

            if (lesson.getCourse().getId() != courseId) {
                throw new IllegalArgumentException("Lesson " + lessonId + " does not belong to course " + courseId);
            }

            lesson.setLessonOrder(i + 1);
            lessonRepository.save(lesson);
        }
    }

    @Override
    public List<LessonDTO> getPublishedLessonsByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return lessonRepository.findByCourseIdAndIsPublishedTrueOrderByLessonOrderAsc(courseId)
                .stream()
                .map(LessonDTO::new).collect(Collectors.toList());
    }

    @Override
    public long countLessonsByCourse(Long courseId) {
        return lessonRepository.countByCourseId(courseId);
    }
}
