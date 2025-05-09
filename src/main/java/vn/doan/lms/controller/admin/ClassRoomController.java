package vn.doan.lms.controller.admin;

import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import vn.doan.lms.service.implements_class.ClassRoomService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@AllArgsConstructor
@RestController
public class ClassRoomController {
    private final ClassRoomService classRoomService;

    @GetMapping("/admin/classrooms")
    public ResponseEntity<List<String>> getAllClass() {
        List<String> classrooms = classRoomService.getAllClassName();
        return ResponseEntity.ok(classrooms);
    }

}
