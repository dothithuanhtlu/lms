package vn.doan.lms.controller.admin;

import org.springframework.web.bind.annotation.RestController;
import lombok.AllArgsConstructor;
import vn.doan.lms.service.implements_class.ClassRoomService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.doan.lms.domain.dto.ClassRoomDetailDTO;
import vn.doan.lms.domain.dto.ClassRoomListDTO;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/admin")
public class ClassRoomController {
    private final ClassRoomService classRoomService;

    @GetMapping("/classroom/{classId}")
    public ResponseEntity<ClassRoomDetailDTO> getClassRoomDetail(@PathVariable long classId) {
        return ResponseEntity.ok(classRoomService.getClassRoomDetail(classId));
    }

    @GetMapping("/classrooms")
    public ResponseEntity<List<ClassRoomListDTO>> getAllClassRooms() {
        return ResponseEntity.ok(classRoomService.getAllClassRooms());
    }

    @GetMapping("/classrooms/name")
    public ResponseEntity<List<String>> getAllClassNames() {
        List<String> classrooms = classRoomService.getAllClassName();
        return ResponseEntity.ok(classrooms);
    }
}
