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
import vn.doan.lms.domain.AssignmentComment;
import vn.doan.lms.domain.AssignmentDocument;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.AssignmentCommentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentCommentDTO;
import vn.doan.lms.domain.dto.AssignmentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentDTO;
import vn.doan.lms.domain.dto.AssignmentUpdateDTO;
import vn.doan.lms.domain.dto.CreateAssignmentWithFilesRequest;
import vn.doan.lms.domain.dto.Meta;
import vn.doan.lms.domain.dto.ResultPaginationDTO;
import vn.doan.lms.repository.AssignmentCommentRepository;
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
    private final UserRepository userRepository;
    private final AssignmentCommentRepository assignmentCommentRepository;
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
    public AssignmentDTO updateAssignment(Long assignmentId, AssignmentUpdateDTO updateDTO) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        if (updateDTO.getCourseId() != null) {
            Course course = courseRepository.findById(updateDTO.getCourseId()).orElseThrow(
                    () -> new ResourceNotFoundException("Course not found with id: " + updateDTO.getCourseId()));
            assignment.setCourse(course);
        }
        List<AssignmentDocument> assignmentDocuments = assignment.getDocuments();
        if (updateDTO.getFileUploadsNew() != null) {
            @SuppressWarnings("rawtypes")
            List<Map> fileUploadResults = cloudinaryService.uploadMultipleFiles(updateDTO.getFileUploadsNew(),
                    "assignments/" + updateDTO.getTitle() + "/");
            for (@SuppressWarnings("rawtypes")
            Map fileUploadResult : fileUploadResults) {
                String filePath = (String) fileUploadResult.get("secure_url");
                String originalFileName = (String) fileUploadResult.get("name_file_original");

                // Create and save AssignmentDocument entity
                AssignmentDocument assignmentDocument = new AssignmentDocument();
                assignmentDocument.setFileNameOriginal(originalFileName);
                assignmentDocument.setFilePath(filePath);
                assignmentDocument.setAssignment(assignment);
                assignmentDocumentRepository.save(assignmentDocument);
            }
        }
        if (updateDTO.getFileDeleteIds() != null) {
            for (Long documentId : updateDTO.getFileDeleteIds()) {
                AssignmentDocument document = assignmentDocumentRepository.findById(documentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
                assignmentDocuments.remove(document);
                assignmentDocumentRepository.delete(document);
            }
        }

        assignment.setTitle(updateDTO.getTitle());
        assignment.setDescription(updateDTO.getDescription());
        assignment.setDueDate(updateDTO.getDueDate());
        assignment.setMaxScore(updateDTO.getMaxScore());
        assignment.setAllowLateSubmission(
                updateDTO.getAllowLateSubmission() != null ? updateDTO.getAllowLateSubmission() : false);
        assignment.setIsPublished(updateDTO.getIsPublished() != null ? updateDTO.getIsPublished() : false);
        assignment.setDocuments(assignmentDocuments);

        Assignment savedAssignment = assignmentRepository.save(assignment);
        return new AssignmentDTO(savedAssignment);
    }

    @Override
    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + assignmentId);
        }
        assignmentRepository.deleteById(assignmentId);
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
    public AssignmentCommentDTO createAssignmentComment(Long assignmentId, AssignmentCommentCreateDTO commentDTO) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email.equals("anonymousUser")) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + email));
        AssignmentComment comment = new AssignmentComment();
        comment.setContent(commentDTO.getContent());
        comment.setUser(user);
        comment.setAssignment(assignment);
        AssignmentComment savedComment = assignmentCommentRepository.save(comment);
        return new AssignmentCommentDTO(savedComment);
    }

    @Override
    public List<AssignmentCommentDTO> getCommentsByAssignmentId(Long assignmentId) {
        return assignmentCommentRepository.findByAssignmentId(assignmentId)
                .stream()
                .map(AssignmentCommentDTO::new)
                .toList();
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
                log.info("✅ File uploaded successfully to Cloudinary: {}", secureUrl);

                // Create document entity with available fields only
                AssignmentDocument document = new AssignmentDocument();
                document.setFileNameOriginal(file.getOriginalFilename());
                document.setFilePath(secureUrl);
                document.setAssignment(assignment);

                documents.add(assignmentDocumentRepository.save(document));
                log.info("✅ Document saved to database: {} for assignment ID: {}", document.getFileNameOriginal(),
                        assignment.getId());
            } else {
                log.error("❌ Failed to upload file: {} - Error: {}", file.getOriginalFilename(),
                        uploadResult.get("message"));
            }
        }

        return documents;
    }

}