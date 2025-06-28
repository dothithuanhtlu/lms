package vn.doan.lms.controller;

import java.util.Map;

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
import vn.doan.lms.domain.dto.ResultPaginationDTO;
import vn.doan.lms.domain.dto.SubmissionCreateRequest;
import vn.doan.lms.domain.dto.SubmissionResponse;
import vn.doan.lms.domain.dto.SubmissionStatistics;
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
    public ResponseEntity<Object> getSubmissionsByAssignment(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestParam(name = "current", defaultValue = "1") int currentOptional,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSizeOptional) {

        ResultPaginationDTO submissions = submissionService.getSubmissionsByAssignment(assignmentId,
                currentOptional - 1, pageSizeOptional);
        return ResponseEntity.ok(submissions);
    }

    // Get student's own submissions
    @GetMapping("/my-submissions")
    public ResponseEntity<Object> getMySubmissions(
            @RequestParam(name = "courseId", required = false) Long courseId,
            @RequestParam(name = "current", defaultValue = "1") int currentOptional,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSizeOptional,
            Authentication authentication) {

        String username = authentication.getName();
        ResultPaginationDTO submissions = submissionService.getSubmissionsByStudent(username, courseId,
                currentOptional - 1, pageSizeOptional);
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

    // Get submission statistics for an assignment (teacher only)
    @GetMapping("/assignment/{assignmentId}/statistics")
    public ResponseEntity<SubmissionStatistics> getSubmissionStatistics(
            @PathVariable("assignmentId") Long assignmentId,
            Authentication authentication) {

        try {
            log.info("Getting submission statistics for assignment ID: {}", assignmentId);

            SubmissionStatistics statistics = submissionService.getSubmissionStatistics(assignmentId);

            log.info("Statistics retrieved successfully for assignment ID: {}", assignmentId);
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("Error getting submission statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Submit or update assignment submission (unified endpoint)
     * Automatically determines whether to create new or update existing submission
     */
    @PostMapping(value = "/submit-or-update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitOrUpdateAssignment(
            @Valid @ModelAttribute SubmissionCreateRequest request,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            Authentication authentication) {

        try {
            log.info("Student submitting or updating assignment ID: {} with {} files",
                    request.getAssignmentId(), files != null ? files.length : 0);

            String username = authentication.getName();

            SubmissionResponse response = submissionService.submitOrUpdateSubmission(request, files, username);

            log.info("Submission processed successfully with ID: {}", response.getId());
            return ResponseEntity.ok(response);

        } catch (BadRequestExceptionCustom e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error processing submission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process submission: " + e.getMessage());
        }
    }

    /**
     * Get count of unsubmitted assignments that student can still submit
     * (assignments that are either not due yet OR overdue but allow late
     * submission)
     */
    @GetMapping("/student/{studentId}/unsubmitted-count")
    public ResponseEntity<?> getUnsubmittedAssignmentCount(
            @PathVariable("studentId") Long studentId,
            Authentication authentication) {

        try {
            log.info("Getting submittable unsubmitted assignment count for student ID: {}", studentId);

            long count = submissionService.getUnsubmittedAssignmentCountByStudentId(studentId);

            log.info("Found {} submittable unsubmitted assignments for student ID: {}", count, studentId);
            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "unsubmittedCount", count,
                    "message", String.format("Student has %d assignments that can still be submitted", count)));

        } catch (Exception e) {
            log.error("Error getting unsubmitted assignment count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get unsubmitted assignment count: " + e.getMessage());
        }
    }
}
