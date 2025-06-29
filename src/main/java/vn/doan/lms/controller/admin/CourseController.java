package vn.doan.lms.controller.admin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.dto.CourseCreateDTO;
import vn.doan.lms.domain.dto.CourseDTO;
import vn.doan.lms.domain.dto.CourseDTOInfo;
import vn.doan.lms.domain.dto.CourseDetailDTO;
import vn.doan.lms.domain.dto.CourseFullDetailDTO;
import vn.doan.lms.domain.dto.StudentCourseDetailDTO;
import vn.doan.lms.service.implements_class.CourseService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
@Slf4j
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

    @GetMapping("/student/courses/{courseId}/details")
    public ResponseEntity<StudentCourseDetailDTO> getStudentCourseDetails(
            @PathVariable("courseId") Long courseId,
            Authentication authentication) {

        try {
            log.info("Getting course details for courseId: {} by student: {}", courseId, authentication.getName());

            String studentUsername = authentication.getName();
            StudentCourseDetailDTO courseDetails = courseService.getStudentCourseDetails(courseId, studentUsername);

            log.info("Successfully retrieved course details for courseId: {}", courseId);
            return ResponseEntity.ok(courseDetails);

        } catch (Exception e) {
            log.error("Error getting course details for courseId: {} by student: {}",
                    courseId, authentication.getName(), e);
            throw e;
        }
    }

    @PutMapping("/courses/{courseId}/students/{studentId}/scores")
    public ResponseEntity<Void> updateStudentScores(
            @PathVariable("courseId") Long courseId,
            @PathVariable("studentId") Long studentId,
            @RequestBody vn.doan.lms.domain.dto.UpdateStudentScoreRequest request) {
        courseService.updateStudentScores(courseId, studentId, request.getMidtermScore(), request.getFinalScore());
        return ResponseEntity.ok().build();
    }

}
