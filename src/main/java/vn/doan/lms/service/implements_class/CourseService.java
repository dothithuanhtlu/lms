package vn.doan.lms.service.implements_class;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.Enrollment;
import vn.doan.lms.domain.dto.CourseDTO;
import vn.doan.lms.domain.dto.CourseDTOInfo;
import vn.doan.lms.domain.dto.CourseDetailDTO;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.EnrollmentRepository;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;

    public CourseDTOInfo getInforCourses() {
        long totalCourse = courseRepository.count();

        LocalDate today = LocalDate.now();

        long countCourseEnded = courseRepository.countByEndDateBefore(today);
        long countCourseUpcoming = courseRepository.countByStartDateAfter(today);
        long countCourseActive = courseRepository.countByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);

        return new CourseDTOInfo(totalCourse, countCourseActive, countCourseEnded, countCourseUpcoming);
    }

    public List<CourseDTO> getCoursesBySubjectId(long subjectId) {
        if (!courseRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("No courses found for subject with id: " + subjectId);
        }
        return courseRepository.findAllBySubjectIdWithEnrollments(subjectId).stream()
                .map(CourseDTO::new)
                .collect(Collectors.toList());

    }

    public CourseDetailDTO getCourseDetails(Long courseId) {
        Course course = courseRepository.findByIdWithEnrollments(courseId);
        if (course == null) {
            throw new ResourceNotFoundException("Course not found");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdWithStudent(courseId);

        return new CourseDetailDTO(course, enrollments);
    }

    public void deleteCourseById(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        courseRepository.deleteById(courseId);
    }

    public void updateCourse(CourseDTO courseDTO, long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id");
        }
        Course course = this.courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseDTO.getId()));
        course.setCourseCode(courseDTO.getCourseCode());
        course.setMaxStudents(courseDTO.getMaxStudents());
        course.setStartDate(courseDTO.getStartDate());
        course.setEndDate(courseDTO.getEndDate());
        course.setTeacher(this.userService.getUserByUserCode(courseDTO.getTeacherCode()));
        this.courseRepository.save(course);
    }

    public int getCurrentStudentCount(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        return (int) enrollmentRepository.countByCourseId(courseId);
    }

    public List<CourseDTO> getCoursesByTeacherId(Long teacherId) {
        List<Course> courses = courseRepository.findByTeacher_Id(teacherId);
        return courses.stream().map(CourseDTO::new).collect(Collectors.toList());
    }
}
