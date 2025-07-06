package vn.doan.lms.service.implements_class;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.Assignment;
import vn.doan.lms.domain.AssignmentDocument;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.AssignmentCommentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentDTO;
import vn.doan.lms.domain.dto.CreateAssignmentWithFilesRequest;
import vn.doan.lms.domain.dto.Meta;
import vn.doan.lms.domain.dto.ResultPaginationDTO;
import vn.doan.lms.domain.dto.UpdateAssignmentInfoRequest;
import vn.doan.lms.domain.dto.UpdateAssignmentWithFilesRequest;
import vn.doan.lms.repository.AssignmentDocumentRepository;
import vn.doan.lms.repository.AssignmentRepository;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.service.interfaces.IAssignmentService;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class AssignmentService implements IAssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final CloudinaryService cloudinaryService;
    private final AssignmentDocumentRepository assignmentDocumentRepository;
    private final ModelMapper modelMapper;

    @Override
    public ResultPaginationDTO getAssignmentsByCourseId(Long courseId, int currentOptional, int pageSizeOptional,
            String keyword, Boolean isPublished, LocalDate startDate, LocalDate endDate, Boolean isLateSubmission) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        Pageable pageable = Pageable.ofSize(pageSizeOptional).withPage(currentOptional);
        Page<AssignmentDTO> pageAssignment = assignmentRepository
                .filterAssignments(courseId, keyword, isPublished, startDate, endDate, isPublished, pageable)
                .map(AssignmentDTO::new);
        // Tạo metadata phân trang
        Meta meta = new Meta();
        meta.setPage(pageAssignment.getNumber());
        meta.setPageSize(pageAssignment.getSize());
        meta.setPages(pageAssignment.getTotalPages());
        meta.setTotal(pageAssignment.getTotalElements());

        // Trả về ResultPaginationDTO
        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(pageAssignment.get());
        return result;
    }

    @Override
    @Transactional
    public AssignmentDTO createAssignment(AssignmentCreateDTO createDTO) throws IOException {
        Course course = courseRepository.findById(createDTO.getCourseId()).orElseThrow(
                () -> new ResourceNotFoundException("Course not found with id: " + createDTO.getCourseId()));

        Assignment assignment = new Assignment();
        assignment.setTitle(createDTO.getTitle());
        assignment.setDescription(createDTO.getDescription());
        assignment.setCourse(course);
        assignment.setDueDate(createDTO.getDueDate());
        assignment.setMaxScore(createDTO.getMaxScore());
        assignment.setAllowLateSubmission(
                createDTO.getAllowLateSubmission() != null ? createDTO.getAllowLateSubmission() : false);
        assignment.setIsPublished(createDTO.getIsPublished() != null ? createDTO.getIsPublished() : false);
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());

        Assignment savedAssignment = assignmentRepository.save(assignment);
        return new AssignmentDTO(savedAssignment);
    }

    @Override
    @Transactional
    public AssignmentDTO updateAssignment(Long assignmentId, UpdateAssignmentInfoRequest updateDTO) {
        if (this.assignmentRepository.existsById(assignmentId) == false) {
            throw new ResourceNotFoundException("Assignment not found with id: " + assignmentId);
        }
        // Find the assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        // Update only the information fields (no files involved)
        assignment.setTitle(updateDTO.getTitle());
        assignment.setDueDate(updateDTO.getDueDate());
        assignment.setDescription(updateDTO.getDescription());
        assignment.setMaxScore(updateDTO.getMaxScore());
        assignment.setIsPublished(updateDTO.getIsPublished() != null ? updateDTO.getIsPublished() : false);
        assignment.setAllowLateSubmission(
                updateDTO.getAllowLateSubmission() != null ? updateDTO.getAllowLateSubmission() : false);
        assignment.setUpdatedAt(LocalDateTime.now());

        // Save and return
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return new AssignmentDTO(savedAssignment);
    }

    @Override
    @Transactional
    public void deleteAssignment(Long assignmentId) {
        log.info("🗑️ Starting deletion process for assignment ID: {}", assignmentId);

        // 1. Find assignment with all related data
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        try {
            // 2. Delete all assignment files from Cloudinary
            String assignmentFolderPath = "lms/assignments/" + assignmentId;
            log.info("Deleting assignment files from Cloudinary folder: {}", assignmentFolderPath);

            boolean assignmentFilesDeleted = cloudinaryService.deleteFolder(assignmentFolderPath);
            if (assignmentFilesDeleted) {
                log.info("Assignment files deleted successfully from Cloudinary");
            } else {
                log.warn("Failed to delete some assignment files from Cloudinary");
            }

            // 3. Delete all submission files from Cloudinary
            if (!assignment.getSubmissions().isEmpty()) {
                log.info("Found {} submissions to delete files for", assignment.getSubmissions().size());

                for (vn.doan.lms.domain.Submission submission : assignment.getSubmissions()) {
                    String submissionFolderPath = "lms/submissions/" + submission.getId();
                    log.info("Deleting submission files from folder: {}", submissionFolderPath);

                    boolean submissionFilesDeleted = cloudinaryService.deleteFolder(submissionFolderPath);
                    if (submissionFilesDeleted) {
                        log.info("Submission {} files deleted successfully", submission.getId());
                    } else {
                        log.warn("Failed to delete submission {} files", submission.getId());
                    }
                }
            } else {
                log.info("📋 No submissions found for assignment {}", assignmentId);
            }

            // 4. Delete assignment from database (cascade will handle documents and
            // submissions)
            log.info("Deleting assignment from database...");
            assignmentRepository.deleteById(assignmentId);

            log.info("Assignment {} deleted successfully from database", assignmentId);

        } catch (Exception e) {
            log.error("Error during assignment deletion process: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete assignment: " + e.getMessage(), e);
        }

        log.info("Assignment deletion completed successfully for ID: {}", assignmentId);
    }

    @Override
    public AssignmentDTO getAssignmentById(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        return new AssignmentDTO(assignment);
    }

    @Override
    public long countAssignmentsByCourse(Long courseId) {
        return assignmentRepository.countByCourseId(courseId);
    }

    @Override
    public long countPublishedAssignmentsByCourse(Long courseId, Boolean isPublished) {
        return assignmentRepository.countByCourseIdAndIsPublished(courseId, isPublished);
    }

    @Override
    @Transactional
    public AssignmentDTO createAssignmentWithFiles(CreateAssignmentWithFilesRequest request, MultipartFile[] files)
            throws IOException {

        log.info("Creating assignment with title: {} for course: {}", request.getTitle(), request.getCourseId());

        // 1. Validate and get course
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        // 2. Map request to entity using ModelMapper
        Assignment assignment = modelMapper.map(request, Assignment.class);
        assignment.setCourse(course);
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());

        // 3. Save assignment first to get ID
        assignment = assignmentRepository.save(assignment);
        log.info("Assignment created with ID: {}", assignment.getId());

        // 4. Process files if provided
        if (files != null && files.length > 0) {
            List<AssignmentDocument> documents = processAssignmentFiles(assignment, files,
                    request.getDocumentsMetadata());
            assignment.setDocuments(documents);
        }

        // 5. Return DTO response
        return new AssignmentDTO(assignment);
    }

    /**
     * Process assignment files - similar to lesson files processing
     */
    @SuppressWarnings("rawtypes")
    private List<AssignmentDocument> processAssignmentFiles(Assignment assignment, MultipartFile[] files,
            String documentsMetadata) throws IOException {

        log.info("Processing {} files for assignment ID: {}", files.length, assignment.getId());

        // Parse metadata - for now we'll use simple approach since AssignmentDocument
        // is simpler
        // Future: can expand AssignmentDocument to have more fields like LessonDocument

        // Upload files to Cloudinary với đúng folder structure
        String folderName = "lms/assignments/" + assignment.getId();
        log.info("Uploading {} files to Cloudinary folder: {}", files.length, folderName);

        // CloudinaryService returns List<Map> without generic type
        List<Map> uploadResults = cloudinaryService.uploadMultipleFiles(Arrays.asList(files), folderName);

        // Create and save AssignmentDocument entities
        List<AssignmentDocument> documents = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            Map uploadResult = uploadResults.get(i);

            log.info("Processing file {}: {}, size: {} bytes", i + 1, file.getOriginalFilename(), file.getSize());

            if (!uploadResult.containsKey("error")) {
                String secureUrl = (String) uploadResult.get("secure_url");
                log.info("File uploaded successfully to Cloudinary: {}", secureUrl);

                // Create document entity with available fields only
                AssignmentDocument document = new AssignmentDocument();
                document.setFileNameOriginal(file.getOriginalFilename());
                document.setFilePath(secureUrl);
                document.setAssignment(assignment);

                documents.add(assignmentDocumentRepository.save(document));
                log.info("Document saved to database: {} for assignment ID: {}", document.getFileNameOriginal(),
                        assignment.getId());
            } else {
                log.error("Failed to upload file: {} - Error: {}", file.getOriginalFilename(),
                        uploadResult.get("message"));
            }
        }

        return documents;
    }

    @Override
    public void updateStatusAssignment(Long assignmentId, boolean isPublished) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        assignment.setIsPublished(isPublished);
        assignment.setUpdatedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
        log.info("Updated assignment ID: {} - Published status: {}", assignmentId, isPublished);
    }

    @Override
    @Transactional
    public AssignmentDTO updateAssignmentWithFiles(Long assignmentId, UpdateAssignmentWithFilesRequest request,
            MultipartFile[] files) throws IOException {
        log.info("Updating assignment ID: {} with files", assignmentId);

        // Find existing assignment
        Assignment existingAssignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        // Step 1: Delete all existing documents and their files from Cloudinary
        if (existingAssignment.getDocuments() != null && !existingAssignment.getDocuments().isEmpty()) {
            log.info("Deleting {} existing documents for assignment ID: {}",
                    existingAssignment.getDocuments().size(), assignmentId);

            // Delete files from Cloudinary using folder approach
            String folderPath = "lms/assignments/" + assignmentId;
            boolean deleted = cloudinaryService.deleteFolder(folderPath);
            if (deleted) {
                log.info("Successfully deleted assignment folder from Cloudinary: {}", folderPath);
            } else {
                log.warn("Failed to delete some files from Cloudinary folder: {}", folderPath);
            }

            // Delete documents from database
            assignmentDocumentRepository.deleteAll(existingAssignment.getDocuments());
            existingAssignment.getDocuments().clear();
            log.info("Deleted all existing documents from database for assignment ID: {}", assignmentId);
        }

        // Step 2: Update assignment basic information
        updateAssignmentBasicInfo(existingAssignment, request);

        // Step 3: Process new files if provided
        List<AssignmentDocument> newDocuments = new ArrayList<>();
        if (files != null && files.length > 0) {
            log.info("Processing {} new files for assignment ID: {}", files.length, assignmentId);
            newDocuments = processAssignmentFiles(existingAssignment, files, request.getDocumentsMetadata());
            if (existingAssignment.getDocuments() == null) {
                existingAssignment.setDocuments(new ArrayList<>());
            }
            existingAssignment.getDocuments().addAll(newDocuments);
        }

        // Step 4: Save updated assignment
        Assignment savedAssignment = assignmentRepository.save(existingAssignment);

        log.info("Successfully updated assignment ID: {} with {} new documents", assignmentId, newDocuments.size());
        return new AssignmentDTO(savedAssignment);
    }

    // Helper method to update basic assignment info
    private void updateAssignmentBasicInfo(Assignment assignment, UpdateAssignmentWithFilesRequest request) {
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            assignment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }
        if (request.getMaxScore() != null) {
            assignment.setMaxScore(request.getMaxScore());
        }
        if (request.getDueDate() != null && !request.getDueDate().trim().isEmpty()) {
            try {
                assignment.setDueDate(LocalDateTime.parse(request.getDueDate()));
            } catch (Exception e) {
                log.warn("Invalid date format for dueDate: {}, keeping existing value", request.getDueDate());
            }
        }
        if (request.getAllowLateSubmission() != null) {
            assignment.setAllowLateSubmission(request.getAllowLateSubmission());
        }
        if (request.getIsPublished() != null) {
            assignment.setIsPublished(request.getIsPublished());
        }

        // Always update timestamp
        assignment.setUpdatedAt(LocalDateTime.now());
    }
}