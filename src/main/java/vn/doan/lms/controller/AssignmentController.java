package vn.doan.lms.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import vn.doan.lms.domain.dto.AssignmentCommentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentCommentDTO;
import vn.doan.lms.domain.dto.AssignmentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentDTO;
import vn.doan.lms.domain.dto.AssignmentUpdateDTO;
import vn.doan.lms.domain.dto.ResultPaginationDTO;
import vn.doan.lms.service.implements_class.AssignmentService;
import vn.doan.lms.service.interfaces.IAssignmentService;

@RestController
@RequestMapping("/api/assignments")
@AllArgsConstructor
public class AssignmentController {

     private final IAssignmentService assignmentService;

     @GetMapping("/course/{courseId}")
     public ResponseEntity<Object> getAssignmentsByCourse(@PathVariable Long courseId, @RequestParam(defaultValue = "1") int currentOptional,
                                                          @RequestParam(defaultValue = "10") int pageSizeOptional,
                                                          @RequestParam(name = "keyword", required = false) String keyword,
                                                          @RequestParam(name = "isPublished", required = false) Boolean isPublished,
                                                          @RequestParam(name = "startDate", required = false) LocalDate startDate,
                                                          @RequestParam(name = "endDate", required = false) LocalDate endDate,
                                                          @RequestParam(name = "isLateSubmission", required = false) Boolean isLateSubmission
                                                          ) {
     ResultPaginationDTO assignments = assignmentService.getAssignmentsByCourseId(courseId, currentOptional -1 , pageSizeOptional, keyword, isPublished, startDate, endDate, isLateSubmission);
     return ResponseEntity.ok(assignments);
     }

     @GetMapping("/detail/{assignmentId}")
     public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable Long assignmentId) {
         AssignmentDTO assignment = assignmentService.getAssignmentById(assignmentId);
         return ResponseEntity.ok(assignment);
     }

     @PostMapping(value = "/create")
     public ResponseEntity<AssignmentDTO> createAssignment(@Valid @ModelAttribute
                                                           AssignmentCreateDTO createDTO) throws IOException {
         AssignmentDTO createdAssignment = assignmentService.createAssignment(createDTO);
         return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
     }

     @PutMapping("/update/{assignmentId}")
     public ResponseEntity<AssignmentDTO> updateAssignment(@PathVariable Long assignmentId, @Valid @ModelAttribute AssignmentUpdateDTO updateDTO) {
        AssignmentDTO updatedAssignment = assignmentService.updateAssignment(assignmentId, updateDTO);
        return ResponseEntity.ok(updatedAssignment);
     }

     @DeleteMapping("/delete/{assignmentId}")
     public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId) {
         assignmentService.deleteAssignment(assignmentId);
         return ResponseEntity.ok().build();
     }

     @GetMapping("/course/{courseId}/count")
     public ResponseEntity<Long> countAssignments(@PathVariable Long courseId) {
     long count = assignmentService.countAssignmentsByCourse(courseId);
     return ResponseEntity.ok(count);
     }

     @GetMapping("/course/{courseId}/count/isPublished")
     public ResponseEntity<Long> countPublishedAssignments(@PathVariable Long courseId, @RequestParam("isPublished") Boolean isPublished) {
         long count = assignmentService.countPublishedAssignmentsByCourse(courseId, isPublished);
         return ResponseEntity.ok(count);
     }

     @PostMapping("/add-comment/{assignmentId}")
     public ResponseEntity<Object> addComment(@PathVariable Long assignmentId, @Valid @RequestBody AssignmentCommentCreateDTO createDTO) {
        AssignmentCommentDTO comment = assignmentService.createAssignmentComment(assignmentId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
     }

     @GetMapping("/comments/{assignmentId}")
     public ResponseEntity<List<AssignmentCommentDTO>> getCommentsByAssignmentId(@PathVariable Long assignmentId) {
         List<AssignmentCommentDTO> comments = assignmentService.getCommentsByAssignmentId(assignmentId);
         return ResponseEntity.ok(comments);
     }
}
