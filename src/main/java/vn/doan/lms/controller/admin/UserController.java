package vn.doan.lms.controller.admin;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.user_dto.AdminDTO;
import vn.doan.lms.domain.dto.user_dto.StudentDTO;
import vn.doan.lms.domain.dto.user_dto.StudentDTOUpdate;
import vn.doan.lms.domain.dto.user_dto.TeacherDTO;
import vn.doan.lms.domain.dto.user_dto.UserDTO;
import vn.doan.lms.domain.dto.user_dto.UserDTOCreate;
import vn.doan.lms.domain.dto.user_dto.UserStatisticsDTO;
import vn.doan.lms.domain.dto.user_dto.UserUpdateDTO;
import vn.doan.lms.service.implements_class.UserService;
import vn.doan.lms.util.error.EmailValidationException;
import vn.doan.lms.util.error.UserCodeValidationException;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@AllArgsConstructor
public class UserController {
    private static final String ROLE_STUDENT = "Student";
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/admin/students")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> students = this.userService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @PostMapping("/admin/students")
    public ResponseEntity<StudentDTOUpdate> createStudent(
            @Valid @RequestBody StudentDTOUpdate studentDTOUpdate)
            throws UserCodeValidationException, EmailValidationException {

        studentDTOUpdate.setRoleName(ROLE_STUDENT); // Đảm bảo roleName là STUDENT
        String hashPassword = this.passwordEncoder.encode(studentDTOUpdate.getPassword());
        studentDTOUpdate.setPassword(hashPassword);

        StudentDTOUpdate newStudent = this.userService.createStudent(studentDTOUpdate);
        return ResponseEntity.status(HttpStatus.CREATED).body(newStudent);
    }

    @DeleteMapping("/admin/student/{userCode}")
    public ResponseEntity<Void> deleteStudent(@PathVariable("userCode") String userCode)
            throws UserCodeValidationException {
        this.userService.deleteStudent(userCode);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/admin/student/{userCode}")
    public ResponseEntity<StudentDTO> getStudentByUserCode(@PathVariable("userCode") String userCode)
            throws UserCodeValidationException {
        StudentDTO studentDTO = this.userService.getStudentByUserCode(userCode);
        return ResponseEntity.ok(studentDTO);
    }

    @GetMapping("/admin/users/statistics")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics() {

        return ResponseEntity.ok(this.userService.getUserStatistics());
    }

    @GetMapping("/admin/users")
    public ResponseEntity<Object> getAllUsers(
            @RequestParam("current") Optional<String> currentOptional,
            @RequestParam("pageSize") Optional<String> pageSizeOptional) {
        return ResponseEntity.ok(this.userService.getAllUsers(currentOptional, pageSizeOptional));
    }

    @GetMapping("/admin/teachers")
    public ResponseEntity<List<TeacherDTO>> getAllTeachers() {
        List<TeacherDTO> teachers = userService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/admin/teacher/{userCode}")
    public ResponseEntity<TeacherDTO> getTeacherByUserCode(@PathVariable("userCode") String userCode) {
        TeacherDTO teacher = userService.getTeacherByUserCode(userCode);
        return ResponseEntity.ok(teacher);
    }

    @PutMapping("/admin/user/{userCode}")
    public ResponseEntity<UserUpdateDTO> updateUser(
            @PathVariable("userCode") String userCode,
            @Valid @RequestBody UserUpdateDTO userDTOUpdate) {
        UserUpdateDTO updatedUser = this.userService.updateUser(userCode, userDTOUpdate);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/admin/admin/{userCode}")
    public ResponseEntity<AdminDTO> getAdminByUserCode(@PathVariable("userCode") String userCode) {
        AdminDTO admin = userService.getAdminByUserCode(userCode);
        return ResponseEntity.ok(admin);
    }

    @PostMapping("/admin/user")
    public ResponseEntity<UserDTOCreate> postMethodName(@Valid @RequestBody UserDTOCreate userDTOCreate) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.saveUser(userDTOCreate));
    }

}

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
