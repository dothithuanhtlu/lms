package vn.doan.lms.service.implements_class;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import vn.doan.lms.domain.dto.SubmissionStatistics;
import vn.doan.lms.repository.AssignmentRepository;
import vn.doan.lms.repository.SubmissionDocumentRepository;
import vn.doan.lms.repository.SubmissionRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.service.interfaces.ISubmissionService;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class SubmissionService implements ISubmissionService {

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
        if (submission.getScore() != null) {
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
    @Override
    public SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request, String username) {
        log.info("Grading submission ID: {} by teacher: {}", submissionId, username);

        // 1. Get submission
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        // 2. Get teacher user for validation
        User teacher = userRepository.findOneByUserCode(username);
        if (teacher == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        // 3. Validate teacher permission - only assignment teacher can grade
        Assignment assignment = submission.getAssignment();
        String assignmentTeacher = assignment.getCourse().getTeacher().getUserCode();
        if (!assignmentTeacher.equals(username)) {
            throw new BadRequestExceptionCustom("Only the course teacher can grade this submission");
        }

        // 4. Check if score is within assignment max score
        if (request.getScore() != null && assignment.getMaxScore() != null &&
                request.getScore() > assignment.getMaxScore()) {
            throw new BadRequestExceptionCustom(
                    String.format("Score %.2f exceeds assignment max score %.2f",
                            request.getScore(), assignment.getMaxScore()));
        }

        // 5. Update submission with grade
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        // Status remains SUBMITTED or LATE, grading is indicated by score being
        // non-null
        submission.setGradedAt(LocalDateTime.now());

        submissionRepository.save(submission);

        List<SubmissionDocument> documents = submissionDocumentRepository.findBySubmissionId(submission.getId());
        return mapToSubmissionResponse(submission, documents);
    }

    // ✨ Get submission by ID
    @Override
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
    @Override
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
        if (submission.getScore() != null) {
            throw new BadRequestExceptionCustom("Cannot delete a graded submission.");
        }

        // Delete files from Cloudinary
        deleteSubmissionFiles(submission);

        // Delete submission
        submissionRepository.delete(submission);
        log.info("Submission deleted: {}", submissionId);
    }

    /**
     * Get submission statistics for an assignment
     */
    @Override
    public SubmissionStatistics getSubmissionStatistics(Long assignmentId) {
        log.info("Getting submission statistics for assignment ID: {}", assignmentId);

        // 1. Get assignment and validate
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        // 2. Get all submissions for this assignment
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);

        // 3. Get total students in the course
        Long totalStudents = userRepository.countStudentsByCourseId(assignment.getCourse().getId());

        // 4. Calculate basic statistics
        Long totalSubmissions = (long) submissions.size();
        Long gradedSubmissions = submissions.stream()
                .mapToLong(s -> s.getScore() != null ? 1 : 0)
                .sum();
        Long ungradedSubmissions = totalSubmissions - gradedSubmissions;
        Long lateSubmissions = submissions.stream()
                .mapToLong(s -> Boolean.TRUE.equals(s.getIsLate()) ? 1 : 0)
                .sum();

        // 5. Calculate score statistics
        List<Float> scores = submissions.stream()
                .filter(s -> s.getScore() != null)
                .map(Submission::getScore)
                .toList();

        Float averageScore = null;
        Float highestScore = null;
        Float lowestScore = null;

        if (!scores.isEmpty()) {
            averageScore = (float) scores.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
            highestScore = scores.stream().max(Float::compareTo).orElse(null);
            lowestScore = scores.stream().min(Float::compareTo).orElse(null);
        }

        // 6. Calculate percentages
        Double submissionRate = totalStudents > 0 ? (totalSubmissions * 100.0) / totalStudents : 0.0;
        Double gradingRate = totalSubmissions > 0 ? (gradedSubmissions * 100.0) / totalSubmissions : 0.0;
        Double lateRate = totalSubmissions > 0 ? (lateSubmissions * 100.0) / totalSubmissions : 0.0;

        // 7. Calculate grade distribution
        Float maxScore = assignment.getMaxScore();
        Long excellentGrades = 0L;
        Long goodGrades = 0L;
        Long averageGrades = 0L;
        Long belowAverageGrades = 0L;

        if (maxScore != null && maxScore > 0) {
            for (Float score : scores) {
                if (score != null) {
                    double percentage = (score / maxScore) * 100;
                    if (percentage >= 90)
                        excellentGrades++;
                    else if (percentage >= 80)
                        goodGrades++;
                    else if (percentage >= 70)
                        averageGrades++;
                    else
                        belowAverageGrades++;
                }
            }
        }

        return SubmissionStatistics.builder()
                .totalSubmissions(totalSubmissions)
                .gradedSubmissions(gradedSubmissions)
                .ungradedSubmissions(ungradedSubmissions)
                .lateSubmissions(lateSubmissions)
                .averageScore(averageScore)
                .highestScore(highestScore)
                .lowestScore(lowestScore)
                .assignmentMaxScore(maxScore)
                .submissionRate(submissionRate)
                .gradingRate(gradingRate)
                .lateRate(lateRate)
                .totalStudentsInCourse(totalStudents)
                .studentsNotSubmitted(totalStudents - totalSubmissions)
                .excellentGrades(excellentGrades)
                .goodGrades(goodGrades)
                .averageGrades(averageGrades)
                .belowAverageGrades(belowAverageGrades)
                .build();
    }

    /**
     * Check if a student has submitted for a specific assignment
     */
    @Override
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
                .gradedById(
                        submission.getGradedAt() != null ? submission.getAssignment().getCourse().getTeacher().getId()
                                : null)
                .gradedByName(submission.getGradedAt() != null
                        ? submission.getAssignment().getCourse().getTeacher().getFullName()
                        : null)
                .documents(documentResponses)
                .build();
    }

    /**
     * ✨ Submit or update assignment submission (unified method)
     * Automatically determines whether to create new or update existing submission
     */
    @Transactional
    @Override
    public SubmissionResponse submitOrUpdateSubmission(SubmissionCreateRequest request, MultipartFile[] files,
            String username) throws IOException {
        log.info("Processing submission for assignment ID: {} by user: {}", request.getAssignmentId(), username);

        // 1. Get student user
        User student = userRepository.findOneByUserCode(username);
        if (student == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        // 2. Check if submission already exists
        Optional<Submission> existingSubmission = submissionRepository.findByAssignmentIdAndStudentId(
                request.getAssignmentId(), student.getId());

        if (existingSubmission.isPresent()) {
            // Update existing submission
            log.info("Updating existing submission ID: {}", existingSubmission.get().getId());
            return updateSubmission(existingSubmission.get().getId(), request, files, username);
        } else {
            // Create new submission
            log.info("Creating new submission for assignment ID: {}", request.getAssignmentId());
            return submitAssignment(request, files, username);
        }
    }
}
