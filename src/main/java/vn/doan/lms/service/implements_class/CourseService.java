package vn.doan.lms.service.implements_class;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Assignment;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.Enrollment;
import vn.doan.lms.domain.Subject;
import vn.doan.lms.domain.Submission;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.CourseCreateDTO;
import vn.doan.lms.domain.dto.CourseDTO;
import vn.doan.lms.domain.dto.CourseDTOInfo;
import vn.doan.lms.domain.dto.CourseDetailDTO;
import vn.doan.lms.domain.dto.CourseFullDetailDTO;
import vn.doan.lms.domain.dto.StudentCourseDetailDTO;
import vn.doan.lms.domain.dto.SubjectDTO;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.EnrollmentRepository;
import vn.doan.lms.repository.LessonDocumentRepository;
import vn.doan.lms.repository.QuestionRepository;
import vn.doan.lms.repository.SubmissionDocumentRepository;
import vn.doan.lms.repository.SubmissionRepository;
import vn.doan.lms.repository.SubjectRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.service.interfaces.ICourseService;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class CourseService {
    private static final String TEACHER_ROLE = "Teacher";
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionDocumentRepository submissionDocumentRepository;
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
                long submissionCount = submissionRepository.countByAssignmentId(assignmentId);
                assignment.setTotalSubmissions((int) submissionCount);
            });
        }

        // Populate student assignment submission status
        if (dto.getStudents() != null && dto.getAssignments() != null) {
            dto.getStudents().forEach(student -> {
                List<CourseFullDetailDTO.AssignmentSubmissionStatus> submissionStatuses = new java.util.ArrayList<>();

                dto.getAssignments().forEach(assignment -> {
                    Long assignmentId = assignment.getId();
                    Long studentId = student.getId();

                    // Check if student has submitted this assignment
                    Optional<vn.doan.lms.domain.Submission> submission = submissionRepository
                            .findByAssignmentIdAndStudentId(assignmentId, studentId);

                    String status;
                    java.time.LocalDateTime submissionDate = null;
                    Float score = null;
                    boolean hasSubmitted = submission.isPresent();

                    if (hasSubmitted) {
                        vn.doan.lms.domain.Submission sub = submission.get();
                        submissionDate = sub.getSubmittedAt();
                        score = sub.getScore();

                        // Determine status based on submission details
                        if (submissionDate != null && assignment.getDueDate() != null &&
                                submissionDate.isAfter(assignment.getDueDate())) {
                            status = "LATE";
                        } else {
                            status = "SUBMITTED";
                        }
                    } else {
                        status = "NOT_SUBMITTED";
                    }

                    submissionStatuses.add(CourseFullDetailDTO.AssignmentSubmissionStatus.builder()
                            .assignmentId(assignmentId)
                            .assignmentTitle(assignment.getTitle())
                            .hasSubmitted(hasSubmitted)
                            .submissionDate(submissionDate)
                            .score(score)
                            .status(status)
                            .build());
                });

                student.setAssignmentSubmissions(submissionStatuses);
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

    public StudentCourseDetailDTO getStudentCourseDetails(Long courseId, String studentUsername) {
        // Find the course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Find the student by username
        User student = userRepository.findOneByUserCode(studentUsername);
        if (student == null) {
            throw new ResourceNotFoundException("Student not found with username: " + studentUsername);
        }

        // Verify student is enrolled in the course
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, student.getId())
                .orElseThrow(() -> new BadRequestExceptionCustom("Student is not enrolled in this course"));

        // Build basic course info
        StudentCourseDetailDTO.TeacherInfo teacherInfo = StudentCourseDetailDTO.TeacherInfo.builder()
                .id(course.getTeacher().getId())
                .userCode(course.getTeacher().getUserCode())
                .fullName(course.getTeacher().getFullName())
                .email(course.getTeacher().getEmail())
                .build();

        StudentCourseDetailDTO.StudentPersonalInfo studentInfo = StudentCourseDetailDTO.StudentPersonalInfo.builder()
                .id(student.getId())
                .userCode(student.getUserCode())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .className(student.getClassRoom() != null ? student.getClassRoom().getClassName() : null)
                .enrollmentStatus(enrollment.getStatus())
                .midtermScore(enrollment.getMidtermScore())
                .finalScore(enrollment.getFinalScore())
                .build();

        SubjectDTO subjectDTO = SubjectDTO.builder()
                .id(course.getSubject().getId())
                .subjectCode(course.getSubject().getSubjectCode())
                .subjectName(course.getSubject().getSubjectName())
                .description(course.getSubject().getDescription())
                .build();

        // Get assignments and student's submission status
        List<StudentCourseDetailDTO.StudentAssignmentInfo> assignmentInfos = course.getAssignments().stream()
                .map(assignment -> {
                    // Find student's submission for this assignment
                    Optional<Submission> submissionOpt = submissionRepository
                            .findByAssignmentIdAndStudentId(assignment.getId(), student.getId());

                    StudentCourseDetailDTO.StudentSubmissionInfo submissionInfo;
                    if (submissionOpt.isPresent()) {
                        Submission submission = submissionOpt.get();

                        // Get document count
                        int documentCount = submissionDocumentRepository.countBySubmissionId(submission.getId());

                        // Determine status
                        String status;
                        boolean isLate = false;

                        if (submission.getSubmittedAt() != null && assignment.getDueDate() != null &&
                                submission.getSubmittedAt().isAfter(assignment.getDueDate())) {
                            status = "LATE";
                            isLate = true;
                        } else {
                            status = "SUBMITTED";
                        }

                        // Check if student can edit/delete (typically not allowed after grading or if
                        // late submissions are not allowed)
                        boolean canEdit = submission.getScore() == null &&
                                (assignment.getAllowLateSubmission() ||
                                        assignment.getDueDate() == null ||
                                        LocalDateTime.now().isBefore(assignment.getDueDate()));

                        boolean canDelete = canEdit; // Same logic for delete

                        submissionInfo = StudentCourseDetailDTO.StudentSubmissionInfo.builder()
                                .submissionId(submission.getId())
                                .hasSubmitted(true)
                                .submissionDate(
                                        submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString()
                                                : null)
                                .score(submission.getScore())
                                .feedback(submission.getFeedback())
                                .status(status)
                                .isLate(isLate)
                                .canEdit(canEdit)
                                .canDelete(canDelete)
                                .documentCount(documentCount)
                                .build();
                    } else {
                        // No submission found
                        boolean canSubmit = assignment.getDueDate() == null ||
                                LocalDateTime.now().isBefore(assignment.getDueDate()) ||
                                assignment.getAllowLateSubmission();

                        submissionInfo = StudentCourseDetailDTO.StudentSubmissionInfo.builder()
                                .submissionId(null)
                                .hasSubmitted(false)
                                .submissionDate(null)
                                .score(null)
                                .feedback(null)
                                .status("NOT_SUBMITTED")
                                .isLate(false)
                                .canEdit(canSubmit)
                                .canDelete(false)
                                .documentCount(0)
                                .build();
                    }

                    return StudentCourseDetailDTO.StudentAssignmentInfo.builder()
                            .id(assignment.getId())
                            .title(assignment.getTitle())
                            .description(assignment.getDescription())
                            .maxScore(assignment.getMaxScore())
                            .dueDate(assignment.getDueDate() != null ? assignment.getDueDate().toString() : null)
                            .isPublished(assignment.getIsPublished())
                            .allowLateSubmission(assignment.getAllowLateSubmission())
                            .submission(submissionInfo)
                            .build();
                })
                .collect(Collectors.toList());

        // Get lessons information
        List<StudentCourseDetailDTO.LessonInfo> lessonInfos = course.getLessons().stream()
                .filter(lesson -> lesson.getIsPublished()) // Only show published lessons to students
                .sorted((l1, l2) -> Long.compare(l1.getId(), l2.getId())) // Sort by ID since no lessonOrder field
                .map(lesson -> {
                    // Get lesson documents
                    List<StudentCourseDetailDTO.DocumentInfo> documentInfos = lesson.getDocuments().stream()
                            .map(document -> StudentCourseDetailDTO.DocumentInfo.builder()
                                    .id(document.getId())
                                    .fileName(document.getFileName())
                                    .fileUrl(document.getFilePath()) // Using filePath as fileUrl
                                    .fileSize(null) // LessonDocument doesn't have fileSize
                                    .fileType(document.getDocumentType().name()) // Using enum name
                                    .uploadedAt(
                                            document.getCreatedAt() != null ? document.getCreatedAt().toString() : null)
                                    .build())
                            .collect(Collectors.toList());

                    return StudentCourseDetailDTO.LessonInfo.builder()
                            .id(lesson.getId())
                            .title(lesson.getTitle())
                            .content(null) // Lesson doesn't have content field
                            .description(lesson.getDescription())
                            .lessonOrder(null) // Lesson doesn't have lessonOrder field
                            .isPublished(lesson.getIsPublished())
                            .createdAt(lesson.getCreatedAt() != null ? lesson.getCreatedAt().toString() : null)
                            .updatedAt(lesson.getUpdatedAt() != null ? lesson.getUpdatedAt().toString() : null)
                            .documents(documentInfos)
                            .build();
                })
                .collect(Collectors.toList());

        // Calculate statistics
        List<Submission> studentSubmissions = submissionRepository.findByStudentIdAndAssignmentCourseId(student.getId(),
                courseId);

        int totalAssignments = course.getAssignments().size();
        int submittedAssignments = (int) studentSubmissions.size();
        int gradedAssignments = (int) studentSubmissions.stream().filter(s -> s.getScore() != null).count();
        int lateSubmissions = (int) studentSubmissions.stream()
                .filter(s -> s.getSubmittedAt() != null &&
                        course.getAssignments().stream()
                                .filter(a -> a.getId() == s.getAssignment().getId())
                                .findFirst()
                                .map(Assignment::getDueDate)
                                .map(dueDate -> s.getSubmittedAt().isAfter(dueDate))
                                .orElse(false))
                .count();

        Float averageScore = null;
        if (gradedAssignments > 0) {
            averageScore = (float) studentSubmissions.stream()
                    .filter(s -> s.getScore() != null)
                    .mapToDouble(s -> s.getScore())
                    .average()
                    .orElse(0.0);
        }

        double completionRate = totalAssignments > 0 ? (double) submittedAssignments / totalAssignments * 100 : 0.0;

        StudentCourseDetailDTO.StudentCourseStatistics statistics = StudentCourseDetailDTO.StudentCourseStatistics
                .builder()
                .totalAssignments(totalAssignments)
                .submittedAssignments(submittedAssignments)
                .gradedAssignments(gradedAssignments)
                .lateSubmissions(lateSubmissions)
                .averageScore(averageScore)
                .completionRate(completionRate)
                .build();

        // Build final DTO
        return StudentCourseDetailDTO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getSubject() != null ? course.getSubject().getSubjectName() : null)
                .description(course.getSubject() != null ? course.getSubject().getDescription() : null)
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .maxStudents(course.getMaxStudents())
                .currentStudents(course.getEnrollments() != null ? course.getEnrollments().size() : 0)
                .status(course.getEndDate() != null && course.getEndDate().isBefore(LocalDate.now()) ? "Completed"
                        : course.getStartDate() != null && course.getStartDate().isAfter(LocalDate.now()) ? "Upcoming"
                                : "Active")
                .subject(subjectDTO)
                .teacher(teacherInfo)
                .studentInfo(studentInfo)
                .assignments(assignmentInfos)
                .lessons(lessonInfos)
                .statistics(statistics)
                .build();
    }
}
