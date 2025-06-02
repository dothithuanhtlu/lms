package vn.doan.lms.controller.admin;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Department;
import vn.doan.lms.domain.Subject;
import vn.doan.lms.domain.dto.CourseDTO;
import vn.doan.lms.domain.dto.DepartmentDTO;
import vn.doan.lms.domain.dto.MajorDTO;
import vn.doan.lms.domain.dto.SubjectDTO;
import vn.doan.lms.domain.dto.user_dto.TeacherDTO;
import vn.doan.lms.service.implements_class.CourseService;
import vn.doan.lms.service.implements_class.DepartmentService;
import vn.doan.lms.service.implements_class.MajorService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@AllArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final MajorService majorService;
    private final CourseService courseService;

    @GetMapping("/admin/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartment() {

        return ResponseEntity.ok(this.departmentService.getAllDepartments());
    }

    @PostMapping("/admin/departments")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody DepartmentDTO depart) {
        return ResponseEntity.ok(this.departmentService.createDepartment(depart));
    }

    @GetMapping("/admin/department/{departmentId}/teachers")
    public ResponseEntity<List<TeacherDTO>> getTeachersByDepartmentId(@PathVariable("departmentId") Long departmentId) {
        return ResponseEntity.ok(departmentService.getAllTeacherByDepartmentId(departmentId));
    }

    @GetMapping("/admin/department/{departmentId}/majors")
    public ResponseEntity<List<MajorDTO>> getMajorByDepartmentId(@PathVariable("departmentId") Long departmentId) {
        return ResponseEntity.ok(this.departmentService.getMajorDTOsByDepartmentId(departmentId));
    }

    @GetMapping("/admin/department/majors/{majorId}/subjects")
    public ResponseEntity<List<SubjectDTO>> getMajorByMajorId(@PathVariable("majorId") Long majorId) {
        return ResponseEntity.ok(this.departmentService.getSubjectsByMajorId(majorId));
    }

    @GetMapping("/admin/department/major/{majorId}/name")
    public ResponseEntity<String> getMajorNameByMajorId(@PathVariable("majorId") Long majorId) {
        return ResponseEntity.ok(this.majorService.getMajorNameByMajorId(majorId));
    }

    @GetMapping("/admin/department/{departmentId}/name")
    public ResponseEntity<String> getDepartmentNameByDepartmentId(@PathVariable("departmentId") long departmentId) {
        return ResponseEntity.ok(this.departmentService.getDepartmentNameByDepartmentId(departmentId));
    }

    @GetMapping("/admin/department/major/subjects/{subjectId}/courses")
    public ResponseEntity<List<CourseDTO>> getCoursesBySubjectId(@PathVariable("subjectId") Long subjectId) {
        return ResponseEntity.ok(this.courseService.getCoursesBySubjectId(subjectId));
    }

}
