package vn.doan.lms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Assignment;
import vn.doan.lms.service.implements_class.AssignmentService;

@RestController
@RequestMapping("/api/assignments")
@AllArgsConstructor
public class AssignmentController {

    // private final AssignmentService assignmentService;

    // @GetMapping("/course/{courseId}")
    // public ResponseEntity<List<AssignmentDTO>>
    // getAssignmentsByCourse(@PathVariable Long courseId) {
    // List<AssignmentDTO> assignments =
    // assignmentService.getAssignmentsByCourseId(courseId);
    // return ResponseEntity.ok(assignments);
    // }

    // @GetMapping("/course/{courseId}/published")
    // public ResponseEntity<List<AssignmentDTO>>
    // getPublishedAssignmentsByCourse(@PathVariable Long courseId) {
    // List<AssignmentDTO> assignments =
    // assignmentService.getPublishedAssignmentsByCourseId(courseId);
    // return ResponseEntity.ok(assignments);
    // }

    // @GetMapping("/course/{courseId}/type")
    // public ResponseEntity<List<AssignmentDTO>> getAssignmentsByType(
    // @PathVariable Long courseId,
    // @RequestParam Assignment.AssignmentType type) {
    // List<AssignmentDTO> assignments =
    // assignmentService.getAssignmentsByType(courseId, type);
    // return ResponseEntity.ok(assignments);
    // }

    // @GetMapping("/{assignmentId}")
    // public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable Long
    // assignmentId) {
    // AssignmentDTO assignment = assignmentService.getAssignmentById(assignmentId);
    // return ResponseEntity.ok(assignment);
    // }

    // @PostMapping
    // public ResponseEntity<AssignmentDTO> createAssignment(@Valid @RequestBody
    // AssignmentCreateDTO createDTO) {
    // AssignmentDTO createdAssignment =
    // assignmentService.createAssignment(createDTO);
    // return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
    // }

    // @PutMapping("/{assignmentId}")
    // public ResponseEntity<AssignmentDTO> updateAssignment(
    // @PathVariable Long assignmentId,
    // @Valid @RequestBody AssignmentCreateDTO updateDTO) {
    // AssignmentDTO updatedAssignment =
    // assignmentService.updateAssignment(assignmentId, updateDTO);
    // return ResponseEntity.ok(updatedAssignment);
    // }

    // @DeleteMapping("/{assignmentId}")
    // public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId)
    // {
    // assignmentService.deleteAssignment(assignmentId);
    // return ResponseEntity.ok().build();
    // }

    // @PutMapping("/{assignmentId}/publish")
    // public ResponseEntity<Void> publishAssignment(@PathVariable Long
    // assignmentId) {
    // assignmentService.publishAssignment(assignmentId);
    // return ResponseEntity.ok().build();
    // }

    // @PutMapping("/{assignmentId}/unpublish")
    // public ResponseEntity<Void> unpublishAssignment(@PathVariable Long
    // assignmentId) {
    // assignmentService.unpublishAssignment(assignmentId);
    // return ResponseEntity.ok().build();
    // }

    // @GetMapping("/course/{courseId}/count")
    // public ResponseEntity<Long> countAssignments(@PathVariable Long courseId) {
    // long count = assignmentService.countAssignmentsByCourse(courseId);
    // return ResponseEntity.ok(count);
    // }

    // @GetMapping("/course/{courseId}/count/published")
    // public ResponseEntity<Long> countPublishedAssignments(@PathVariable Long
    // courseId) {
    // long count = assignmentService.countPublishedAssignmentsByCourse(courseId);
    // return ResponseEntity.ok(count);
    // }
}
