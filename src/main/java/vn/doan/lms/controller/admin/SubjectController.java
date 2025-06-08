package vn.doan.lms.controller.admin;

import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.dto.SubjectDTO;
import vn.doan.lms.service.implements_class.SubjectService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@AllArgsConstructor
public class SubjectController {
    private final SubjectService subjectService;

    @GetMapping("/admin/majors/{majorId}/subjects")
    public ResponseEntity<List<SubjectDTO>> getSubjectByMajorId(@PathVariable("majorId") long majorId) {
        return ResponseEntity.ok(subjectService.getSubjectDTOsByMajorId(majorId));
    }

    @GetMapping("/admin/subjects/{subjectId}")
    public ResponseEntity<SubjectDTO> getSubjectById(@PathVariable("subjectId") long subjectId) {
        return ResponseEntity.ok(this.subjectService.getSubjectBySubjectId(subjectId));
    }

}
