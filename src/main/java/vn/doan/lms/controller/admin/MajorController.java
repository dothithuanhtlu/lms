package vn.doan.lms.controller.admin;

import org.springframework.web.bind.annotation.RestController;
import lombok.AllArgsConstructor;
import vn.doan.lms.service.implements_class.MajorService;
import vn.doan.lms.domain.dto.MajorDTO;
import vn.doan.lms.domain.dto.MajorRequestDTO;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class MajorController {
    private final MajorService majorService;

    @GetMapping("/majors")
    public ResponseEntity<List<MajorDTO>> getAllMajors() {
        return ResponseEntity.ok(majorService.getAllMajors());
    }

    @GetMapping("/majors/department/{departmentId}")
    public ResponseEntity<List<MajorDTO>> getMajorsByDepartmentId(@PathVariable Long departmentId) {
        return ResponseEntity.ok(majorService.getMajorDTOsByDepartmentId(departmentId));
    }

    @GetMapping("/majors/{departmentId}/allresponse")
    public ResponseEntity<List<MajorRequestDTO>> getMajorsByDepartmentIdRequest(@PathVariable Long departmentId) {
        return ResponseEntity.ok(majorService.getMajorRequestDTOsByDepartmentId(departmentId));
    }
}