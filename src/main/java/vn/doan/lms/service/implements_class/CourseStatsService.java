package vn.doan.lms.service.implements_class;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.dto.CourseStatsDTO;
import vn.doan.lms.repository.AssignmentRepository;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.EnrollmentRepository;
import vn.doan.lms.repository.LessonRepository;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class CourseStatsService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;

    public CourseStatsDTO getCourseStatistics(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        long currentStudents = enrollmentRepository.countByCourseId(courseId);
        long totalLessons = lessonRepository.countByCourseId(courseId);
        long publishedLessons = lessonRepository.countByCourseIdAndIsPublishedTrue(courseId);
        long totalAssignments = assignmentRepository.countByCourseId(courseId);
        long publishedAssignments = assignmentRepository.countByCourseIdAndIsPublishedTrue(courseId);

        return CourseStatsDTO.builder()
                .courseId(courseId)
                .currentStudents((int) currentStudents)
                .totalLessons((int) totalLessons)
                .publishedLessons((int) publishedLessons)
                .totalAssignments((int) totalAssignments)
                .publishedAssignments((int) publishedAssignments)
                .completionRate(calculateCompletionRate(totalLessons, publishedLessons))
                .build();
    }

    private double calculateCompletionRate(long totalLessons, long publishedLessons) {
        if (totalLessons == 0) {
            return 0.0;
        }
        return (double) publishedLessons / totalLessons * 100.0;
    }
}
