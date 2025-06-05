package vn.doan.lms.controller.admin;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Department;
import vn.doan.lms.domain.dto.CourseDTO;
import vn.doan.lms.domain.dto.CourseDetailDTO;
import vn.doan.lms.domain.dto.DepartmentDTO;
import vn.doan.lms.domain.dto.MajorDTO;
import vn.doan.lms.domain.dto.SubjectDTO;
import vn.doan.lms.domain.dto.user_dto.TeacherDTO;
import vn.doan.lms.domain.dto.user_dto.TeacherSelectDTO;
import vn.doan.lms.service.implements_class.CourseService;
import vn.doan.lms.service.implements_class.DepartmentService;
import vn.doan.lms.service.implements_class.MajorService;
import vn.doan.lms.service.implements_class.SubjectService;
import vn.doan.lms.service.implements_class.UserService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@AllArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final MajorService majorService;
    private final CourseService courseService;
    private final SubjectService subjectService;
    private final UserService userService;

    // Lay toan bo department
    @GetMapping("/admin/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartment() {
        return ResponseEntity.ok(this.departmentService.getAllDepartments());
    }

    // Lay danh sach giao vien theo id khoa
    @GetMapping("/admin/department/{departmentId}/teachers")
    public ResponseEntity<List<TeacherDTO>> getTeachersByDepartmentId(@PathVariable("departmentId") Long departmentId) {
        return ResponseEntity.ok(departmentService.getAllTeacherByDepartmentId(departmentId));
    }

    // Lay danh sach chuyen nganh theo id khoa
    @GetMapping("/admin/department/{departmentId}/majors")
    public ResponseEntity<List<MajorDTO>> getMajorByDepartmentId(@PathVariable("departmentId") Long departmentId) {
        return ResponseEntity.ok(this.majorService.getMajorDTOsByDepartmentId(departmentId));
    }

    // Lay danh sach mon hoc theo id chuyen nganh
    @GetMapping("/admin/department/majors/{majorId}/subjects")
    public ResponseEntity<List<SubjectDTO>> getSubjectByMajorId(@PathVariable("majorId") Long majorId) {
        return ResponseEntity.ok(this.subjectService.getSubjectsByMajorId(majorId));
    }

    // Lay ten chuyen nganh theo id
    @GetMapping("/admin/department/major/{majorId}/name")
    public ResponseEntity<String> getMajorNameByMajorId(@PathVariable("majorId") Long majorId) {
        return ResponseEntity.ok(this.majorService.getMajorNameByMajorId(majorId));
    }

    // Lay ten khoa theo id
    @GetMapping("/admin/department/{departmentId}/name")
    public ResponseEntity<String> getDepartmentNameByDepartmentId(@PathVariable("departmentId") long departmentId) {
        return ResponseEntity.ok(this.departmentService.getDepartmentNameByDepartmentId(departmentId));
    }

    // lấy toàn bộ tên khoa
    @GetMapping("/admin/departments/names")
    public ResponseEntity<List<String>> getAllDepartmentNames() {
        return ResponseEntity.ok(this.departmentService.getAllDepartmentNames());
    }

    // Lay danh sach lop hoc phan theo id mon hoc
    @GetMapping("/admin/department/major/subjects/{subjectId}/courses")
    public ResponseEntity<List<CourseDTO>> getCoursesBySubjectId(@PathVariable("subjectId") long subjectId) {
        return ResponseEntity.ok(this.courseService.getCoursesBySubjectId(subjectId));
    }

    // Lay danh sach hoc ky cua cac lop hoc phan theo id mon hoc
    @GetMapping("/admin/department/major/subject/{subjectId}/courses/semesters")
    public ResponseEntity<List<String>> getSemesterBySubjectIdCourse(@PathVariable("subjectId") long subjectId) {
        return ResponseEntity.ok(this.courseService.getSemesterBySubjectIdCourse(subjectId));
    }

    // Lay ten mon hoc theo id
    @GetMapping("/admin/department/major/subject/{subjectId}/name")
    public ResponseEntity<String> getSubjectNameBySubjectId(@PathVariable("subjectId") long subjectId) {
        return ResponseEntity.ok(this.subjectService.getSubjectNameBySubjectId(subjectId));
    }

    // Lay chi tiet lop hoc phan theo id
    @GetMapping("/admin/department/major/subject/course/{courseId}")
    public ResponseEntity<CourseDetailDTO> getCourseByCourseId(@PathVariable("courseId") long courseId) {
        return ResponseEntity.ok(this.courseService.getCourseDetails(courseId));
    }

    // lay giao vien teacherselectdto theo id khoa
    @GetMapping("/admin/department/teachers_select")
    public ResponseEntity<List<TeacherSelectDTO>> getTeachersForSelect() {
        return ResponseEntity.ok(userService.getTeachersForSelect());
    }

    // Tao moi department
    @PostMapping("/admin/departments")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody DepartmentDTO depart) {
        return ResponseEntity.ok(this.departmentService.createDepartment(depart));
    }
}
