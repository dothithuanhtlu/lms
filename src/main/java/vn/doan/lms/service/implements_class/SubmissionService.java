package vn.doan.lms.service.implements_class;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.Assignment;
import vn.doan.lms.domain.Submission;
import vn.doan.lms.domain.SubmissionDocument;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.Submission.SubmissionStatus;
import vn.doan.lms.domain.dto.GradeSubmissionRequest;
import vn.doan.lms.domain.dto.SubmissionCreateRequest;
import vn.doan.lms.domain.dto.SubmissionDocumentResponse;
import vn.doan.lms.domain.dto.SubmissionResponse;
import vn.doan.lms.repository.AssignmentRepository;
import vn.doan.lms.repository.SubmissionDocumentRepository;
import vn.doan.lms.repository.SubmissionRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionDocumentRepository submissionDocumentRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    // ✨ Student submit assignment
    @Transactional
    public SubmissionResponse submitAssignment(SubmissionCreateRequest request, MultipartFile[] files, String username)
            throws IOException {
        log.info("Processing submission for assignment ID: {} by user: {}", request.getAssignmentId(), username); // 1.
                                                                                                                  // Get
                                                                                                                  // student
                                                                                                                  // user
        User student = userRepository.findOneByUserCode(username);
        if (student == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        // 2. Get assignment
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment not found with id: " + request.getAssignmentId()));

        // 3. Check if student already submitted
        if (submissionRepository.existsByAssignmentIdAndStudentId(assignment.getId(), student.getId())) {
            throw new BadRequestExceptionCustom("Student has already submitted this assignment. Use update instead.");
        }

        // 4. Check if assignment is still accepting submissions
        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            if (!assignment.getAllowLateSubmission()) {
                throw new BadRequestExceptionCustom(
                        "Assignment submission deadline has passed and late submissions are not allowed.");
            }
        } // 5. Create submission
        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .submittedAt(LocalDateTime.now())
                .status(SubmissionStatus.SUBMITTED)
                .isLate(assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate()))
                .build();

        // 6. Save submission first to get ID
        submission = submissionRepository.save(submission);
        log.info("Submission created with ID: {}", submission.getId());

        // 7. Process files if provided
        List<SubmissionDocument> documents = new ArrayList<>();
        if (files != null && files.length > 0) {
            documents = processSubmissionFiles(submission, files, request.getDocumentsMetadata());
        }

        return mapToSubmissionResponse(submission, documents);
    }

    // ✨ Student update submission
    @Transactional
    public SubmissionResponse updateSubmission(Long submissionId, SubmissionCreateRequest request,
            MultipartFile[] files, String username) throws IOException {

        log.info("Updating submission ID: {} by user: {}", submissionId, username);

        // 1. Get submission
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId)); // 2.
                                                                                                                    // Get
                                                                                                                    // student
                                                                                                                    // user
        User student = userRepository.findOneByUserCode(username);
        if (student == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        } // 3. Check if user owns this submission
        if (submission.getStudent().getId() != student.getId()) {
            throw new BadRequestExceptionCustom("You can only update your own submissions.");
        }

        // 4. Check if submission can be updated (not graded yet)
        if (submission.getStatus() == SubmissionStatus.GRADED) {
            throw new BadRequestExceptionCustom("Cannot update a graded submission.");
        } // 5. Update submission timestamp
        submission.setSubmittedAt(LocalDateTime.now()); // Update submission time

        // 6. Delete old files and upload new ones if provided
        List<SubmissionDocument> documents = new ArrayList<>();
        if (files != null && files.length > 0) {
            // Delete old documents
            deleteSubmissionFiles(submission);

            // Process new files
            documents = processSubmissionFiles(submission, files, request.getDocumentsMetadata());
        } else {
            // Keep existing documents
            documents = submissionDocumentRepository.findBySubmissionId(submission.getId());
        }

        submissionRepository.save(submission);
        return mapToSubmissionResponse(submission, documents);
    }

    // ✨ Teacher grade submission
    @Transactional
    public SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request, String username) {
        log.info("Grading submission ID: {} by teacher: {}", submissionId, username);

        // 1. Get submission
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId)); // 2.
                                                                                                                    // Get
                                                                                                                    // teacher
                                                                                                                    // user
        User teacher = userRepository.findOneByUserCode(username);
        if (teacher == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        // 3. Check if score is within assignment max score
        Assignment assignment = submission.getAssignment();
        if (request.getScore() != null && assignment.getMaxScore() != null &&
                request.getScore() > assignment.getMaxScore()) {
            throw new BadRequestExceptionCustom(
                    String.format("Score %.2f exceeds assignment max score %.2f",
                            request.getScore(), assignment.getMaxScore()));
        }

        // 4. Update submission with grade
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(teacher);

        submissionRepository.save(submission);

        List<SubmissionDocument> documents = submissionDocumentRepository.findBySubmissionId(submission.getId());
        return mapToSubmissionResponse(submission, documents);
    }

    // ✨ Get submission by ID
    public SubmissionResponse getSubmissionById(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        List<SubmissionDocument> documents = submissionDocumentRepository.findBySubmissionId(submission.getId());
        return mapToSubmissionResponse(submission, documents);
    }

    // ✨ Get submissions by assignment (for teacher)
    public List<SubmissionResponse> getSubmissionsByAssignment(Long assignmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId, pageable);

        return submissions.getContent().stream()
                .map(submission -> {
                    List<SubmissionDocument> documents = submissionDocumentRepository
                            .findBySubmissionId(submission.getId());
                    return mapToSubmissionResponse(submission, documents);
                })
                .toList();
    } // ✨ Get submissions by student

    public List<SubmissionResponse> getSubmissionsByStudent(String username, Long courseId, int page, int size) {
        User student = userRepository.findOneByUserCode(username);
        if (student == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<Submission> submissions;

        if (courseId != null) {
            // Find submissions by course - need to join with assignment to get course
            submissions = submissionRepository.findByStudentIdAndAssignmentCourseId(student.getId(), courseId,
                    pageable);
        } else {
            submissions = submissionRepository.findByStudentId(student.getId(), pageable);
        }

        return submissions.getContent().stream()
                .map(submission -> {
                    List<SubmissionDocument> documents = submissionDocumentRepository
                            .findBySubmissionId(submission.getId());
                    return mapToSubmissionResponse(submission, documents);
                })
                .toList();
    }

    // ✨ Delete submission
    @Transactional
    public void deleteSubmission(Long submissionId, String username) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));
        User user = userRepository.findOneByUserCode(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        // Check if user owns this submission or is teacher
        if (submission.getStudent().getId() != user.getId()) {
            // Add role check here if needed (teacher can delete any submission)
            throw new BadRequestExceptionCustom("You can only delete your own submissions.");
        }

        // Check if submission can be deleted (not graded yet)
        if (submission.getStatus() == SubmissionStatus.GRADED) {
            throw new BadRequestExceptionCustom("Cannot delete a graded submission.");
        }

        // Delete files from Cloudinary
        deleteSubmissionFiles(submission);

        // Delete submission
        submissionRepository.delete(submission);
        log.info("Submission deleted: {}", submissionId);
    } // ✨ Check if student has submitted assignment

    public boolean hasStudentSubmitted(Long assignmentId, String username) {
        User student = userRepository.findOneByUserCode(username);
        if (student == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        return submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, student.getId());
    }

    // Helper methods
    private List<SubmissionDocument> processSubmissionFiles(Submission submission, MultipartFile[] files,
            String documentsMetadata) throws IOException {

        log.info("Processing {} files for submission ID: {}", files.length, submission.getId()); // Upload files to
                                                                                                 // Cloudinary
        String folderName = "lms/submissions/" + submission.getId();
        List<Map> uploadResults = cloudinaryService.uploadMultipleFiles(Arrays.asList(files), folderName);

        List<SubmissionDocument> documents = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            Map uploadResult = uploadResults.get(i);

            if (!uploadResult.containsKey("error")) {
                String secureUrl = (String) uploadResult.get("secure_url");

                SubmissionDocument document = SubmissionDocument.builder()
                        .fileName(file.getOriginalFilename())
                        .filePath(secureUrl)
                        .fileType(file.getContentType())
                        .fileSize(file.getSize())
                        .submission(submission)
                        .createdAt(LocalDateTime.now())
                        .build();

                documents.add(submissionDocumentRepository.save(document));
                log.info("Document saved: {} for submission ID: {}", document.getFileName(), submission.getId());
            } else {
                log.error("Failed to upload file: {} - Error: {}", file.getOriginalFilename(),
                        uploadResult.get("message"));
            }
        }

        return documents;
    }

    private void deleteSubmissionFiles(Submission submission) {
        List<SubmissionDocument> documents = submissionDocumentRepository.findBySubmissionId(submission.getId());

        for (SubmissionDocument doc : documents) {
            try {
                // Delete from Cloudinary
                String publicId = extractPublicIdFromUrl(doc.getFilePath());
                if (publicId != null) {
                    cloudinaryService.deleteFile(publicId, "raw");
                }
            } catch (Exception e) {
                log.warn("Failed to delete file from Cloudinary: {} - {}", doc.getFileName(), e.getMessage());
            }
        }

        // Delete from database
        submissionDocumentRepository.deleteBySubmissionId(submission.getId());
        log.info("Deleted all files for submission ID: {}", submission.getId());
    }

    private String extractPublicIdFromUrl(String url) {
        try {
            String[] parts = url.split("/");
            if (parts.length >= 6) {
                int uploadIndex = -1;
                for (int i = 0; i < parts.length; i++) {
                    if ("upload".equals(parts[i])) {
                        uploadIndex = i;
                        break;
                    }
                }

                if (uploadIndex > 0 && uploadIndex < parts.length - 1) {
                    StringBuilder publicId = new StringBuilder();
                    for (int i = uploadIndex + 1; i < parts.length; i++) {
                        if (i > uploadIndex + 1) {
                            publicId.append("/");
                        }
                        String part = parts[i];
                        if (i == parts.length - 1) {
                            int dotIndex = part.lastIndexOf('.');
                            if (dotIndex > 0) {
                                part = part.substring(0, dotIndex);
                            }
                        }
                        publicId.append(part);
                    }
                    return publicId.toString();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {} - {}", url, e.getMessage());
            return null;
        }
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission, List<SubmissionDocument> documents) {
        List<SubmissionDocumentResponse> documentResponses = documents.stream()
                .map(doc -> SubmissionDocumentResponse.builder()
                        .id(doc.getId())
                        .fileName(doc.getFileName())
                        .filePath(doc.getFilePath())
                        .fileType(doc.getFileType())
                        .fileSize(doc.getFileSize())
                        .isDownloadable(doc.getIsDownloadable())
                        .build())
                .toList();
        return SubmissionResponse.builder()
                .id(submission.getId())
                .submittedAt(submission.getSubmittedAt())
                .gradedAt(submission.getGradedAt())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .status(submission.getStatus())
                .isLate(submission.getIsLate())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .assignmentMaxScore(submission.getAssignment().getMaxScore())
                .studentId(submission.getStudent().getId()).studentName(submission.getStudent().getFullName())
                .studentEmail(submission.getStudent().getEmail())
                .gradedById(submission.getGradedBy() != null ? submission.getGradedBy().getId() : null)
                .gradedByName(submission.getGradedBy() != null ? submission.getGradedBy().getFullName() : null)
                .documents(documentResponses)
                .build();
    }
}
