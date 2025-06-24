package vn.doan.lms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.dto.GradeSubmissionRequest;
import vn.doan.lms.domain.dto.SubmissionCreateRequest;
import vn.doan.lms.domain.dto.SubmissionResponse;
import vn.doan.lms.service.implements_class.SubmissionService;
import vn.doan.lms.util.error.BadRequestExceptionCustom;

@RestController
@RequestMapping("/api/submissions")
@AllArgsConstructor
@Slf4j
public class SubmissionController {

    private final SubmissionService submissionService;

    // Student submit assignment
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitAssignment(
            @Valid @ModelAttribute SubmissionCreateRequest request,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            Authentication authentication) {

        try {
            log.info("Student submitting assignment ID: {} with {} files",
                    request.getAssignmentId(), files != null ? files.length : 0);

            // Get current user ID (student)
            String username = authentication.getName();

            SubmissionResponse response = submissionService.submitAssignment(request, files, username);

            log.info("Assignment submitted successfully with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error submitting assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to submit assignment: " + e.getMessage());
        }
    }

    // Student update submission (if allowed)
    @PutMapping(value = "/{submissionId}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateSubmission(
            @PathVariable("submissionId") Long submissionId,
            @Valid @ModelAttribute SubmissionCreateRequest request,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            Authentication authentication) {

        try {
            log.info("Student updating submission ID: {} with {} files",
                    submissionId, files != null ? files.length : 0);

            String username = authentication.getName();

            SubmissionResponse response = submissionService.updateSubmission(submissionId, request, files, username);

            log.info("Submission updated successfully with ID: {}", response.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update submission: " + e.getMessage());
        }
    }

    // Teacher grade submission
    @PutMapping("/{submissionId}/grade")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable("submissionId") Long submissionId,
            @Valid @RequestBody GradeSubmissionRequest request,
            Authentication authentication) {

        try {
            log.info("Grading submission ID: {} with score: {}", submissionId, request.getScore());

            String username = authentication.getName();

            SubmissionResponse response = submissionService.gradeSubmission(submissionId, request, username);

            log.info("Submission graded successfully with ID: {}", response.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error grading submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to grade submission: " + e.getMessage());
        }
    }

    // Get submission by ID
    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionResponse> getSubmissionById(@PathVariable("submissionId") Long submissionId) {
        SubmissionResponse response = submissionService.getSubmissionById(submissionId);
        return ResponseEntity.ok(response);
    }

    // Get submissions by assignment (for teacher)
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByAssignment(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<SubmissionResponse> submissions = submissionService.getSubmissionsByAssignment(assignmentId, page, size);
        return ResponseEntity.ok(submissions);
    }

    // Get student's own submissions
    @GetMapping("/my-submissions")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        List<SubmissionResponse> submissions = submissionService.getSubmissionsByStudent(username, courseId, page,
                size);
        return ResponseEntity.ok(submissions);
    }

    // Delete submission (if allowed)
    @DeleteMapping("/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(
            @PathVariable("submissionId") Long submissionId,
            Authentication authentication) {

        String username = authentication.getName();
        submissionService.deleteSubmission(submissionId, username);
        return ResponseEntity.ok().build();
    }

    // Check if student has submitted assignment
    @GetMapping("/check/{assignmentId}")
    public ResponseEntity<Boolean> hasStudentSubmitted(
            @PathVariable("assignmentId") Long assignmentId,
            Authentication authentication) {

        String username = authentication.getName();
        boolean hasSubmitted = submissionService.hasStudentSubmitted(assignmentId, username);
        return ResponseEntity.ok(hasSubmitted);
    }
}
