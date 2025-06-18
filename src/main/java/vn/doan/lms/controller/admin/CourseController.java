package vn.doan.lms.controller.admin;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.dto.CourseCreateDTO;
import vn.doan.lms.domain.dto.CourseDTO;
import vn.doan.lms.domain.dto.CourseDTOInfo;
import vn.doan.lms.domain.dto.CourseDetailDTO;
import vn.doan.lms.domain.dto.CourseFullDetailDTO;
import vn.doan.lms.service.implements_class.CourseService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class CourseController {
    @Autowired
    private final CourseService courseService;

    @GetMapping("/courses/info")
    public ResponseEntity<CourseDTOInfo> getInforCourse() {
        return ResponseEntity.ok().body(this.courseService.getInforCourses());
    }

    @GetMapping("subjects/{subjectId}/courses")
    public ResponseEntity<List<CourseDTO>> getCoursesBySubjectId(@PathVariable("subjectId") long subjectId) {
        return ResponseEntity.ok(this.courseService.getCoursesBySubjectId(subjectId));
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseCreateDTO courseCreateDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    @GetMapping("/courses/{courseId}/details")
    public ResponseEntity<CourseDetailDTO> getCourseByCourseId(@PathVariable("courseId") long courseId) {
        return ResponseEntity.ok(this.courseService.getCourseDetails(courseId));
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable("courseId") Long courseId) {
        courseService.deleteCourseById(courseId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<Void> updateCourse(@Valid @RequestBody CourseDTO courseDTO,
            @PathVariable("courseId") Long courseId) {
        this.courseService.updateCourse(courseDTO, courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/byteacher/{teacherId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByTeacherId(@PathVariable("teacherId") Long teacherId) {
        return ResponseEntity.ok(courseService.getCoursesByTeacherId(teacherId));
    }

    @GetMapping("/courses/bystudent/{studentId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByStudentId(@PathVariable("studentId") Long studentId) {
        return ResponseEntity.ok(courseService.getCoursesByStudentId(studentId));
    }

    @GetMapping("/courses/{courseId}/full-details")
    public ResponseEntity<CourseFullDetailDTO> getCourseFullDetails(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(courseService.getCourseFullDetails(courseId));
    }

}
