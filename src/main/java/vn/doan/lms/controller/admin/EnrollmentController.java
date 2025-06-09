package vn.doan.lms.controller.admin;

import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Enrollment;
import vn.doan.lms.domain.dto.EnrollmentAddUserDTO;
import vn.doan.lms.domain.dto.EnrollmentDelDTO;
import vn.doan.lms.service.implements_class.EnrollmentService;
import vn.doan.lms.service.implements_class.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@AllArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping("/enrollments")
    public ResponseEntity<Void> postMethodName(@RequestBody EnrollmentAddUserDTO enrollmentAddUserDTO) {
        this.enrollmentService.enrollUserToCourse(enrollmentAddUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PostMapping("/enrollments/delete")
    public ResponseEntity<Void> deleteEnrollment(@RequestBody EnrollmentDelDTO enrollmentDelDTO) {
        this.enrollmentService.deleteEnrollment(enrollmentDelDTO);
        return ResponseEntity.noContent().build();
    }

}
