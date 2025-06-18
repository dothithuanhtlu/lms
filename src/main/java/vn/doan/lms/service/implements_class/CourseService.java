package vn.doan.lms.service.implements_class;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.Enrollment;
import vn.doan.lms.domain.Subject;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.CourseCreateDTO;
import vn.doan.lms.domain.dto.CourseDTO;
import vn.doan.lms.domain.dto.CourseDTOInfo;
import vn.doan.lms.domain.dto.CourseDetailDTO;
import vn.doan.lms.domain.dto.CourseFullDetailDTO;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.EnrollmentRepository;
import vn.doan.lms.repository.LessonDocumentRepository;
import vn.doan.lms.repository.QuestionRepository;
import vn.doan.lms.repository.SubmissionRepository;
import vn.doan.lms.repository.SubjectRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class CourseService {
    private static final String TEACHER_ROLE = "Teacher";
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final LessonDocumentRepository lessonDocumentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public CourseDTO createCourse(CourseCreateDTO createDTO) {
        // Validate start date is before end date
        if (createDTO.getStartDate().isAfter(createDTO.getEndDate())) {
            throw new BadRequestExceptionCustom("Start date must be before end date");
        }

        // Check if course code already exists
        if (courseRepository.existsByCourseCode(createDTO.getCourseCode())) {
            throw new BadRequestExceptionCustom("Course code already exists: " + createDTO.getCourseCode());
        }

        // Validate subject exists
        Subject subject = subjectRepository.findById(createDTO.getSubjectId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Subject not found with id: " + createDTO.getSubjectId()));

        // Validate teacher exists and is actually a teacher
        User teacher = userRepository.findById(createDTO.getTeacherId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Teacher not found with id: " + createDTO.getTeacherId()));

        if (teacher.getRole() == null || !TEACHER_ROLE.equals(teacher.getRole().getNameRole())) {
            throw new BadRequestExceptionCustom("User with id " + createDTO.getTeacherId() + " is not a teacher");
        }

        // Create new course
        Course course = Course.builder()
                .courseCode(createDTO.getCourseCode())
                .subject(subject)
                .teacher(teacher)
                .maxStudents(createDTO.getMaxStudents())
                .startDate(createDTO.getStartDate())
                .endDate(createDTO.getEndDate())
                .build();

        Course savedCourse = courseRepository.save(course);
        return new CourseDTO(savedCourse);
    }

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

    public CourseFullDetailDTO getCourseFullDetails(Long courseId) {
        // Fetch course with basic info, subject, teacher, and enrollments
        Course course = courseRepository.findByIdWithFullDetails(courseId);
        if (course == null) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        // Fetch lessons separately to avoid MultipleBagFetchException
        Course courseWithLessons = courseRepository.findByIdWithLessons(courseId);
        if (courseWithLessons != null && courseWithLessons.getLessons() != null) {
            course.setLessons(courseWithLessons.getLessons());
        }

        // Fetch assignments separately to avoid MultipleBagFetchException
        Course courseWithAssignments = courseRepository.findByIdWithAssignments(courseId);
        if (courseWithAssignments != null && courseWithAssignments.getAssignments() != null) {
            course.setAssignments(courseWithAssignments.getAssignments());
        }

        CourseFullDetailDTO dto = new CourseFullDetailDTO(course);

        // Populate lesson document counts separately
        if (dto.getLessons() != null) {
            dto.getLessons().forEach(lesson -> {
                Long lessonId = lesson.getId();
                long documentCount = lessonDocumentRepository.countByLessonId(lessonId);
                lesson.setTotalDocuments((int) documentCount);
            });
        }

        // Populate assignment question and submission counts separately
        if (dto.getAssignments() != null) {
            dto.getAssignments().forEach(assignment -> {
                Long assignmentId = assignment.getId();
                long questionCount = questionRepository.countByAssignmentId(assignmentId);
                long submissionCount = submissionRepository.countByAssignmentId(assignmentId);
                assignment.setTotalQuestions((int) questionCount);
                assignment.setTotalSubmissions((int) submissionCount);
            });
        }

        return dto;
    }

    public List<CourseDTO> getCoursesByTeacherId(Long teacherId) {
        List<Course> courses = courseRepository.findByTeacher_Id(teacherId);
        return courses.stream().map(CourseDTO::new).collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByStudentId(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdWithCourse(studentId);
        return enrollments.stream()
                .map(enrollment -> new CourseDTO(enrollment.getCourse()))
                .collect(Collectors.toList());
    }
}
