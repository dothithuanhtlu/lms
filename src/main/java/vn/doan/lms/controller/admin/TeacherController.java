package vn.doan.lms.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.dto.user_dto.TeacherDTO;
import vn.doan.lms.service.implements_class.UserService;

@RestController
@AllArgsConstructor
public class TeacherController {

    private final UserService userService;

    @GetMapping("/admin/teachers")
    public ResponseEntity<List<TeacherDTO>> getAllTeachers() {
        List<TeacherDTO> teachers = userService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }

}
