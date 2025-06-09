package vn.doan.lms.service.implements_class;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Enrollment;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.EnrollmentAddUserDTO;
import vn.doan.lms.domain.dto.EnrollmentDelDTO;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.EnrollmentRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.util.error.BadRequestExceptionCustom;

@Service
@AllArgsConstructor
public class EnrollmentService {
    private static final String STUDENT_ROLE = "Student";

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public void enrollUserToCourse(EnrollmentAddUserDTO enrollment) {
        if (!this.userRepository.findOneByUserCode(enrollment.getUserCode()).getRole().getNameRole()
                .equals(STUDENT_ROLE)) {
            throw new BadRequestExceptionCustom("Not found student with user code");
        }
        // check user belongs to department
        if (!userRepository.existsByUserCodeAndDepartmentId(enrollment.getUserCode(), enrollment.getDepartmentId())) {
            throw new BadRequestExceptionCustom("User does not belong to the specified department");
        }

        User student = this.userRepository.findOneByUserCode(enrollment.getUserCode());
        if (student == null) {
            throw new BadRequestExceptionCustom("Student not found with user code: ");
        }
        // check in enrollments table have student registered in course
        if (this.enrollmentRepository.existsByCourseIdAndStudentId(enrollment.getCourseId(), student.getId())) {
            throw new BadRequestExceptionCustom("Student already registered in course");
        }
        Enrollment errol = new Enrollment();
        errol.setCourse(this.courseRepository.findOneById(enrollment.getCourseId()));
        errol.setStudent(student);
        errol.setStatus("REGISTERED");
        this.enrollmentRepository.save(errol);
    }

    @Transactional
    public void deleteEnrollment(EnrollmentDelDTO enrollmentDelDTO) {
        this.enrollmentRepository.deleteByStudent_IdAndCourse_Id(enrollmentDelDTO.getStudentId(),
                enrollmentDelDTO.getCourseId());

    }
}
