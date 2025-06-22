package vn.doan.lms.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.dto.AssignmentCommentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentCommentDTO;
import vn.doan.lms.domain.dto.AssignmentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentDTO;
import vn.doan.lms.domain.dto.AssignmentUpdateDTO;
import vn.doan.lms.domain.dto.CreateAssignmentWithFilesRequest;
import vn.doan.lms.domain.dto.ResultPaginationDTO;
import vn.doan.lms.service.interfaces.IAssignmentService;

@RestController
@RequestMapping("/api/assignments")
@AllArgsConstructor
@Slf4j
public class AssignmentController {

    private final IAssignmentService assignmentService;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Object> getAssignmentsByCourse(@PathVariable("courseId") Long courseId,
            @RequestParam(defaultValue = "1") int currentOptional,
            @RequestParam(defaultValue = "10") int pageSizeOptional,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "isPublished", required = false) Boolean isPublished,
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate,
            @RequestParam(name = "isLateSubmission", required = false) Boolean isLateSubmission) {
        ResultPaginationDTO assignments = assignmentService.getAssignmentsByCourseId(courseId, currentOptional - 1,
                pageSizeOptional, keyword, isPublished, startDate, endDate, isLateSubmission);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/detail/{assignmentId}")
    public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable("assignmentId") Long assignmentId) {
        AssignmentDTO assignment = assignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping(value = "/create")
    public ResponseEntity<AssignmentDTO> createAssignment(@Valid @ModelAttribute AssignmentCreateDTO createDTO)
            throws IOException {
        AssignmentDTO createdAssignment = assignmentService.createAssignment(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
    }

    @PutMapping("/update/{assignmentId}")
    public ResponseEntity<AssignmentDTO> updateAssignment(@PathVariable("assignmentId") Long assignmentId,
            @Valid @ModelAttribute AssignmentUpdateDTO updateDTO) {
        AssignmentDTO updatedAssignment = assignmentService.updateAssignment(assignmentId, updateDTO);
        return ResponseEntity.ok(updatedAssignment);
    }

    @DeleteMapping("/delete/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable("assignmentId") Long assignmentId) {
        assignmentService.deleteAssignment(assignmentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> countAssignments(@PathVariable("courseId") Long courseId) {
        long count = assignmentService.countAssignmentsByCourse(courseId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/course/{courseId}/count/isPublished")
    public ResponseEntity<Long> countPublishedAssignments(@PathVariable("courseId") Long courseId,
            @RequestParam("isPublished") Boolean isPublished) {
        long count = assignmentService.countPublishedAssignmentsByCourse(courseId, isPublished);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/add-comment/{assignmentId}")
    public ResponseEntity<Object> addComment(@PathVariable("assignmentId") Long assignmentId,
            @Valid @RequestBody AssignmentCommentCreateDTO createDTO) {
        AssignmentCommentDTO comment = assignmentService.createAssignmentComment(assignmentId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/comments/{assignmentId}")
    public ResponseEntity<List<AssignmentCommentDTO>> getCommentsByAssignmentId(
            @PathVariable("assignmentId") Long assignmentId) {
        List<AssignmentCommentDTO> comments = assignmentService.getCommentsByAssignmentId(assignmentId);
        return ResponseEntity.ok(comments);
    }

    private void validateRequest(CreateAssignmentWithFilesRequest request, MultipartFile[] files) {
        if (files != null && files.length > 10) {
            throw new IllegalArgumentException("Too many files. Maximum 10 files allowed per assignment.");
        }

        if (files != null) {
            for (MultipartFile file : files) {
                if (file.getSize() > 100 * 1024 * 1024) { // 100MB
                    throw new IllegalArgumentException(
                            String.format("File '%s' is too large. Maximum 100MB allowed.",
                                    file.getOriginalFilename()));
                }

                // Log file info để debug
                log.info("Validating file: {} (size: {} bytes, type: {})",
                        file.getOriginalFilename(), file.getSize(), file.getContentType());
            }
        }

        // Log request info
        log.info("Request validation: title={}, courseId={}, files count={}",
                request.getTitle(), request.getCourseId(), files != null ? files.length : 0);
    }

    @PostMapping(value = "/create-with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAssignmentWithFiles(
            @Valid @ModelAttribute CreateAssignmentWithFilesRequest request,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            log.info("Creating assignment with files, title: {}", request.getTitle());

            // Validate request
            validateRequest(request, files);

            // Create assignment with files
            AssignmentDTO assignment = assignmentService.createAssignmentWithFiles(request, files);

            log.info("Assignment created successfully with ID: {}", assignment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(assignment);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating assignment with files: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating assignment: " + e.getMessage());
        }
    }

    @PutMapping("/{assignmentId}/publish")
    public ResponseEntity<Void> updateStatusAssignment(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestParam("isPublished") boolean isPublished) {
        assignmentService.updateStatusAssignment(assignmentId, isPublished);
        return ResponseEntity.ok().build();
    }
}
