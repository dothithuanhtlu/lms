package vn.doan.lms.controller.admin;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.doan.lms.domain.dto.user_dto.StudentDTO;
import vn.doan.lms.domain.dto.user_dto.StudentDTOUpdate;
import vn.doan.lms.service.implements_class.UserService;
import vn.doan.lms.util.error.EmailValidationException;
import vn.doan.lms.util.error.UserCodeValidationException;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class UserController {
    private static final String ROLE_STUDENT = "Student";
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admins/students")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        return ResponseEntity.ok(this.userService.getAllStudents());
    }

    @PostMapping("/admins/students")
    public ResponseEntity<StudentDTOUpdate> createStudent(
            @Valid @RequestBody StudentDTOUpdate studentDTOUpdate)
            throws UserCodeValidationException, EmailValidationException {

        studentDTOUpdate.setRoleName(ROLE_STUDENT); // Đảm bảo roleName là STUDENT
        String hashPassword = this.passwordEncoder.encode(studentDTOUpdate.getPassword());
        studentDTOUpdate.setPassword(hashPassword);

        StudentDTOUpdate newStudent = this.userService.createStudent(studentDTOUpdate);
        return ResponseEntity.status(HttpStatus.CREATED).body(newStudent);
    }

    @DeleteMapping("/admins/students/{userCode}")
    public ResponseEntity<Void> deleteStudent(@PathVariable("userCode") String userCode)
            throws UserCodeValidationException {
        this.userService.deleteStudent(userCode);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/admins/students/{userCode}")
    public ResponseEntity<StudentDTOUpdate> getStudentByUserCode(String userCode) throws UserCodeValidationException {
        StudentDTOUpdate studentDTOUpdate = this.userService.getStudentByUserCode(userCode);
        return ResponseEntity.ok(studentDTOUpdate);
    }

}
// @GetMapping("/users")
// public ResponseEntity<List<UserDTO>> getAllUsers() {
// return ResponseEntity.ok(this.userService.getAllUsers());
// }
// @GetMapping("/users/{userCode}")
// public ResponseEntity<User> getUserById(@PathVariable("userCode") String
// userCode) {
// return ResponseEntity.ok(this.userService.getUserByUserCode(userCode));
// }

// @PostMapping("/users")
// public ResponseEntity<User> postMethodName(@RequestBody User user) throws
// UserCodeValidationException {
// if (this.userService.isExistUserCode(user.getUserCode())) {
// throw new IdValidationException("Id already exist!");
// }
// user.setPassword(this.passwordEncoder.encode(user.getPassword()));
// User newUser = this.userService.saveUser(user);
// return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
// }

// @DeleteMapping("/users/{id}")
// public ResponseEntity<String> deleteMethodName(@PathVariable("userCode")
// String userCode)
// throws IdValidationException {
// if (!this.userService.isExistUserCode(userCode)) {
// throw new IdValidationException("Id not exist!");
// }
// this.userService.deleteUser(userCode);
// return ResponseEntity.ok("Delete successed!");
// }
// @PutMapping("/users/{id}")
// public ResponseEntity<User> putMethodName(@PathVariable String id,
// @RequestBody String entity) {
// //TODO: process PUT request

// return null;
// }
