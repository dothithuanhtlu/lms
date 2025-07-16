package vn.doan.lms.service.implements_class;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.Enrollment;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.EnrollmentAddUserDTO;
import vn.doan.lms.domain.dto.EnrollmentDelDTO;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.EnrollmentRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
@Slf4j
public class EnrollmentService {
    private static final String STUDENT_ROLE = "Student";
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public void enrollUserToCourse(EnrollmentAddUserDTO enrollment) {
        // First, check if user exists
        User student = this.userRepository.findOneByUserCode(enrollment.getUserCode());
        if (student == null) {
            throw new BadRequestExceptionCustom("Student not found with user code: " + enrollment.getUserCode());
        }

        // Check if user is a student
        if (student.getRole() == null || !STUDENT_ROLE.equals(student.getRole().getNameRole())) {
            throw new BadRequestExceptionCustom("User with code " + enrollment.getUserCode() + " is not a student");
        }

        // check user belongs to department
        if (!userRepository.existsByUserCodeAndDepartmentId(enrollment.getUserCode(), enrollment.getDepartmentId())) {
            throw new BadRequestExceptionCustom("User does not belong to the specified department");
        }
        // check in enrollments table have student registered in course
        if (this.enrollmentRepository.existsByCourseIdAndStudentId(enrollment.getCourseId(), student.getId())) {
            throw new BadRequestExceptionCustom("Student already registered in course");
        }
        Enrollment errol = new Enrollment();
        errol.setCourse(this.courseRepository.findById(enrollment.getCourseId())
                .orElseThrow(
                        () -> new BadRequestExceptionCustom("Course not found with id: " + enrollment.getCourseId())));
        errol.setStudent(student);
        errol.setStatus("REGISTERED");
        this.enrollmentRepository.save(errol);
    }

    @Transactional
    public void deleteEnrollment(EnrollmentDelDTO enrollmentDelDTO) {
        this.enrollmentRepository.deleteByStudent_IdAndCourse_Id(enrollmentDelDTO.getStudentId(),
                enrollmentDelDTO.getCourseId());

    }

    public void updateStudentScores(Long courseId, Long studentId, Float midtermScore, Float finalScore) {
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student is not enrolled in this course"));
        if (midtermScore != null) {
            enrollment.setMidtermScore(midtermScore);
        }
        if (finalScore != null) {
            enrollment.setFinalScore(finalScore);
        }

        // Cập nhật status dựa trên điểm số
        updateEnrollmentStatus(enrollment);

        enrollmentRepository.save(enrollment);
    }

    /**
     * Cập nhật status của enrollment dựa trên điểm số
     * Logic:
     * - Nếu có đủ 2 điểm (midterm + final) và điểm trung bình >= 5.0 → COMPLETED
     * - Nếu có đủ 2 điểm (midterm + final) và điểm trung bình < 5.0 → FAILED
     * - Nếu chưa có đủ 2 điểm → giữ nguyên REGISTERED
     */
    private void updateEnrollmentStatus(Enrollment enrollment) {
        Float midtermScore = enrollment.getMidtermScore();
        Float finalScore = enrollment.getFinalScore();

        // Chỉ cập nhật status khi có đủ 2 điểm
        if (midtermScore != null && finalScore != null) {
            float average = (midtermScore + finalScore) / 2.0f;

            if (average >= 40.0f) {
                enrollment.setStatus("COMPLETED");
            } else {
                enrollment.setStatus("FAILED");
            }

            log.info("Updated enrollment status to {} for student {} in course {} with average score: {}",
                    enrollment.getStatus(), enrollment.getStudent().getUserCode(),
                    enrollment.getCourse().getCourseCode(), average);
        }
        // Nếu chưa có đủ 2 điểm, giữ nguyên status hiện tại (thường là REGISTERED)
    }
}
