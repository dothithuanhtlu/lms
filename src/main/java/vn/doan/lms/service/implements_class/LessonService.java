package vn.doan.lms.service.implements_class;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.Lesson;
import vn.doan.lms.domain.LessonDocument;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.CreateLessonWithFilesRequest;
import vn.doan.lms.domain.dto.DocumentMetadata;
import vn.doan.lms.domain.dto.LessonResponse;
import vn.doan.lms.domain.dto.UpdateLessonRequest;
import vn.doan.lms.domain.dto.UpdateLessonWithFilesRequest;
import vn.doan.lms.domain.dto.UpdateLessonRequest;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.LessonDocumentRepository;
import vn.doan.lms.repository.LessonRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.service.interfaces.ILessonService;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final LessonDocumentRepository lessonDocumentRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper; // ← Added ModelMapper

    public LessonResponse createLessonWithFiles(CreateLessonWithFilesRequest request,
            MultipartFile[] files) throws IOException {

        log.info("Creating lesson with title: {} for course: {}", request.getTitle(), request.getCourseId());

        // 1. Validate and get course
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        // 2. Map request to entity using ModelMapper
        Lesson lesson = modelMapper.map(request, Lesson.class);
        lesson.setCourse(course);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        // 3. Save lesson first to get ID
        lesson = lessonRepository.save(lesson);
        log.info("Lesson created with ID: {}", lesson.getId());

        // 4. Process files if provided
        if (files != null && files.length > 0) {
            List<LessonDocument> documents = processLessonFiles(lesson, files, request.getDocumentsMetadata());
            lesson.setDocuments(documents);
        }

        // 5. Map entity to response using ModelMapper
        return mapToLessonResponse(lesson);
    }

    public void deleteLesson(Long lessonId) {
        log.info("🗑️ Deleting lesson with ID: {}", lessonId);

        // 1. Get lesson with documents
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        log.info("📚 Found lesson: {} with {} documents", lesson.getTitle(), lesson.getDocuments().size());

        // 2. Delete entire folder from Cloudinary (Primary method)
        String folderPath = "lessons/" + lessonId;

        // Try the prefix-based deletion first (more reliable)
        boolean folderDeleted = cloudinaryService.deleteFolderByPrefix(folderPath);

        if (!folderDeleted) {
            log.warn("⚠️ Prefix deletion failed, trying folder deletion method");
            folderDeleted = cloudinaryService.deleteFolder(folderPath);
        }

        if (!folderDeleted) {
            log.warn("⚠️ Both Cloudinary deletion methods failed, trying individual file deletion");

            // 3. Fallback: Delete individual files from Cloudinary
            if (lesson.getDocuments() != null && !lesson.getDocuments().isEmpty()) {
                for (LessonDocument document : lesson.getDocuments()) {
                    try {
                        String publicId = extractPublicIdFromUrl(document.getFilePath());
                        if (publicId != null) {
                            cloudinaryService.deleteFile(publicId, document.getResourceType());
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to delete individual file: {} - {}", document.getFileName(),
                                e.getMessage());
                    }
                }
            }
        }

        // 4. Delete lesson from database (CASCADE will delete documents)
        lessonRepository.delete(lesson);
        log.info("✅ Successfully deleted lesson {} from database", lessonId);

        // 5. Final verification log
        log.info("✅ Lesson deletion completed - ID: {}, Title: {}, Cloudinary folder: {}",
                lessonId, lesson.getTitle(), folderPath);
    }

    // ✨ Helper method to extract public ID from Cloudinary URL
    private String extractPublicIdFromUrl(String cloudinaryUrl) {
        try {
            if (cloudinaryUrl == null || !cloudinaryUrl.contains("cloudinary.com")) {
                return null;
            }

            // Example URL:
            // https://res.cloudinary.com/cloud/raw/upload/v123/lessons/1/filename.pdf
            // Extract: lessons/1/filename

            String[] parts = cloudinaryUrl.split("/");
            boolean foundUpload = false;
            StringBuilder publicId = new StringBuilder();

            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i])) {
                    foundUpload = true;
                    continue;
                }

                if (foundUpload && i < parts.length - 1) {
                    // Skip version if present (starts with 'v')
                    if (parts[i + 1].startsWith("v") && parts[i + 1].matches("v\\d+")) {
                        continue;
                    }

                    if (publicId.length() > 0) {
                        publicId.append("/");
                    }
                    publicId.append(parts[i + 1]);
                }
            }

            // Remove file extension
            String result = publicId.toString();
            int lastDot = result.lastIndexOf('.');
            if (lastDot > 0) {
                result = result.substring(0, lastDot);
            }

            log.debug("🔍 Extracted public ID: {} from URL: {}", result, cloudinaryUrl);
            return result;

        } catch (Exception e) {
            log.error("❌ Failed to extract public ID from URL: {} - {}", cloudinaryUrl, e.getMessage());
            return null;
        }
    }

    private List<LessonDocument> processLessonFiles(Lesson lesson, MultipartFile[] files,
            String documentsMetadata) throws IOException {

        log.info("Processing {} files for lesson ID: {}", files.length, lesson.getId());

        // Parse metadata
        List<DocumentMetadata> metadataList = parseDocumentsMetadata(documentsMetadata, files.length);

        // Upload files to Cloudinary
        String folderName = "lessons/" + lesson.getId();
        log.info("Uploading {} files to Cloudinary folder: {}", files.length, folderName);
        List<Map> uploadResults = cloudinaryService.uploadMultipleFiles(Arrays.asList(files), folderName);

        // Create and save LessonDocument entities
        List<LessonDocument> documents = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            DocumentMetadata metadata = i < metadataList.size() ? metadataList.get(i)
                    : DocumentMetadata.builder()
                            .documentType(determineDocumentType(file))
                            .isDownloadable(true)
                            .build();

            Map uploadResult = uploadResults.get(i);

            if (!uploadResult.containsKey("error")) {
                // Create document entity
                LessonDocument document = LessonDocument.builder()
                        .fileName(file.getOriginalFilename())
                        .filePath((String) uploadResult.get("secure_url"))
                        .documentType(metadata.getDocumentType())
                        .isDownloadable(metadata.getIsDownloadable())
                        .lesson(lesson)
                        .createdAt(LocalDateTime.now())
                        .build();

                documents.add(lessonDocumentRepository.save(document));
                log.info("Document saved: {} for lesson ID: {}", document.getFileName(), lesson.getId());
            } else {
                log.error("Failed to upload file: {} - Error: {}", file.getOriginalFilename(),
                        uploadResult.get("message"));
            }
        }

        return documents;
    }

    private List<DocumentMetadata> parseDocumentsMetadata(String documentsMetadata, int filesCount) {
        List<DocumentMetadata> metadataList = new ArrayList<>();

        // Nếu không có metadata, tạo default cho tất cả files
        if (documentsMetadata == null || documentsMetadata.trim().isEmpty()) {
            log.info("No metadata provided, generating defaults for {} files", filesCount);

            // Tạo default metadata cho tất cả files
            for (int i = 0; i < filesCount; i++) {
                metadataList.add(DocumentMetadata.builder()
                        .documentType(LessonDocument.DocumentType.OTHER) // Sẽ được detect sau
                        .isDownloadable(true)
                        .description("Auto-uploaded document")
                        .build());
            }
            return metadataList;
        }

        // Parse metadata nếu có
        try {
            metadataList = objectMapper.readValue(documentsMetadata,
                    new TypeReference<List<DocumentMetadata>>() {
                    });
            log.info("Parsed {} document metadata entries", metadataList.size());
        } catch (Exception e) {
            log.warn("Failed to parse documents metadata, using defaults: {}", e.getMessage());
        }

        // Fill thiếu metadata với defaults
        while (metadataList.size() < filesCount) {
            metadataList.add(DocumentMetadata.builder()
                    .documentType(LessonDocument.DocumentType.OTHER)
                    .isDownloadable(true)
                    .description("Auto-uploaded document")
                    .build());
        }

        return metadataList;
    }

    private LessonDocument.DocumentType determineDocumentType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null)
            return LessonDocument.DocumentType.OTHER;

        String lowerFileName = fileName.toLowerCase();
        String contentType = file.getContentType();

        if (contentType != null) {
            if (contentType.startsWith("video/"))
                return LessonDocument.DocumentType.VIDEO;
            if (contentType.startsWith("image/"))
                return LessonDocument.DocumentType.IMAGE;
            if (contentType.startsWith("audio/"))
                return LessonDocument.DocumentType.AUDIO;
        }

        if (lowerFileName.endsWith(".pdf"))
            return LessonDocument.DocumentType.PDF;
        if (lowerFileName.endsWith(".doc"))
            return LessonDocument.DocumentType.DOC;
        if (lowerFileName.endsWith(".docx"))
            return LessonDocument.DocumentType.DOCX;
        if (lowerFileName.endsWith(".ppt") || lowerFileName.endsWith(".pptx"))
            return LessonDocument.DocumentType.PPT;

        return LessonDocument.DocumentType.OTHER;
    }

    // ✨ Simplified mapping using ModelMapper
    private LessonResponse mapToLessonResponse(Lesson lesson) {
        LessonResponse response = modelMapper.map(lesson, LessonResponse.class);

        // Map documents manually for better control
        if (lesson.getDocuments() != null && !lesson.getDocuments().isEmpty()) {
            List<LessonResponse.DocumentResponse> documentResponses = lesson.getDocuments().stream()
                    .map(doc -> {
                        LessonResponse.DocumentResponse docResponse = modelMapper.map(doc,
                                LessonResponse.DocumentResponse.class);
                        docResponse.setFileUrl(doc.getFilePath()); // Set Cloudinary URL
                        return docResponse;
                    })
                    .collect(Collectors.toList());

            response.setDocuments(documentResponses);
        }

        return response;
    }

    // ✨ Get lesson by ID using ModelMapper
    public LessonResponse getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));

        return mapToLessonResponse(lesson);
    }

    // ✨ Update lesson using ModelMapper
    public LessonResponse updateLesson(Long id, CreateLessonWithFilesRequest request) {
        Lesson existingLesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));

        // Map request to existing entity (skip null values)
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(request, existingLesson);

        // Update timestamp
        existingLesson.setUpdatedAt(LocalDateTime.now());

        // Save and return
        lessonRepository.save(existingLesson);
        return mapToLessonResponse(existingLesson);
    }

    // ✨ Update lesson status
    public void updateStatusLesson(Long lessonId, boolean isPublished) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        lesson.setIsPublished(isPublished);
        lesson.setUpdatedAt(LocalDateTime.now());

        lessonRepository.save(lesson);
        log.info("Updated status of lesson ID: {} to {}", lessonId, isPublished);
    }

    // ✨ Update lesson info only (without files)
    @Transactional
    public LessonResponse updateLessonInfo(Long lessonId, UpdateLessonRequest request) {
        log.info("Updating lesson info for ID: {}", lessonId);

        // Find existing lesson
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        // Update lesson information
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            existingLesson.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existingLesson.setDescription(request.getDescription());
        }
        if (request.getDurationMinutes() != null) {
            existingLesson.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getIsPublished() != null) {
            existingLesson.setIsPublished(request.getIsPublished());
        }

        // Update timestamp
        existingLesson.setUpdatedAt(LocalDateTime.now());

        // Save and return
        Lesson savedLesson = lessonRepository.save(existingLesson);
        log.info("Successfully updated lesson info for ID: {}", lessonId);

        return mapToLessonResponse(savedLesson);
    }

    // ✨ Update lesson with files - Replace all old files
    @Transactional
    public LessonResponse updateLessonWithFiles(Long lessonId, CreateLessonWithFilesRequest request,
            MultipartFile[] files) throws IOException {
        log.info("Updating lesson ID: {} with files", lessonId);

        // Find existing lesson
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        // Step 1: Delete all existing documents and their files from Cloudinary
        if (existingLesson.getDocuments() != null && !existingLesson.getDocuments().isEmpty()) {
            log.info("Deleting {} existing documents for lesson ID: {}", existingLesson.getDocuments().size(),
                    lessonId);

            // Delete files from Cloudinary
            for (LessonDocument doc : existingLesson.getDocuments()) {
                try {
                    String publicId = extractPublicIdFromUrl(doc.getFilePath());
                    if (publicId != null) {
                        cloudinaryService.deleteFile(publicId, doc.getDocumentType().getResourceType());
                        log.info("Deleted file from Cloudinary: {}", doc.getFileName());
                    }
                } catch (Exception e) {
                    log.warn("Failed to delete file from Cloudinary: {} - {}", doc.getFileName(), e.getMessage());
                }
            }

            // Delete documents from database
            lessonDocumentRepository.deleteAll(existingLesson.getDocuments());
            existingLesson.getDocuments().clear();
            log.info("Deleted all existing documents from database for lesson ID: {}", lessonId);
        }

        // Step 2: Update lesson basic information
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            existingLesson.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existingLesson.setDescription(request.getDescription());
        }
        if (request.getDurationMinutes() != null) {
            existingLesson.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getIsPublished() != null) {
            existingLesson.setIsPublished(request.getIsPublished());
        }

        // Update timestamp
        existingLesson.setUpdatedAt(LocalDateTime.now());

        // Step 3: Process new files if provided
        List<LessonDocument> newDocuments = new ArrayList<>();
        if (files != null && files.length > 0) {
            log.info("Processing {} new files for lesson ID: {}", files.length, lessonId);
            newDocuments = processLessonFiles(existingLesson, files, request.getDocumentsMetadata());
            if (existingLesson.getDocuments() == null) {
                existingLesson.setDocuments(new ArrayList<>());
            }
            existingLesson.getDocuments().addAll(newDocuments);
        }

        // Step 4: Save updated lesson
        Lesson savedLesson = lessonRepository.save(existingLesson);

        log.info("Successfully updated lesson ID: {} with {} new documents", lessonId, newDocuments.size());
        return mapToLessonResponse(savedLesson);
    }

    // ✨ Update lesson with files - Overloaded method for
    // UpdateLessonWithFilesRequest
    @Transactional
    public LessonResponse updateLessonWithFiles(Long lessonId, UpdateLessonWithFilesRequest request,
            MultipartFile[] files) throws IOException {
        log.info("Updating lesson ID: {} with files using UpdateRequest", lessonId);

        // Find existing lesson
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        // Step 1: Delete all existing documents and their files from Cloudinary
        if (existingLesson.getDocuments() != null && !existingLesson.getDocuments().isEmpty()) {
            log.info("Deleting {} existing documents for lesson ID: {}", existingLesson.getDocuments().size(),
                    lessonId);

            // Delete files from Cloudinary
            for (LessonDocument doc : existingLesson.getDocuments()) {
                try {
                    String publicId = extractPublicIdFromUrl(doc.getFilePath());
                    if (publicId != null) {
                        cloudinaryService.deleteFile(publicId, doc.getDocumentType().getResourceType());
                        log.info("Deleted file from Cloudinary: {}", doc.getFileName());
                    }
                } catch (Exception e) {
                    log.warn("Failed to delete file from Cloudinary: {} - {}", doc.getFileName(), e.getMessage());
                }
            }

            // Delete documents from database
            lessonDocumentRepository.deleteAll(existingLesson.getDocuments());
            existingLesson.getDocuments().clear();
            log.info("Deleted all existing documents from database for lesson ID: {}", lessonId);
        }

        // Step 2: Update lesson basic information
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            existingLesson.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existingLesson.setDescription(request.getDescription());
        }
        if (request.getDurationMinutes() != null) {
            existingLesson.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getIsPublished() != null) {
            existingLesson.setIsPublished(request.getIsPublished());
        }

        // Update timestamp
        existingLesson.setUpdatedAt(LocalDateTime.now());

        // Step 3: Process new files if provided
        List<LessonDocument> newDocuments = new ArrayList<>();
        if (files != null && files.length > 0) {
            log.info("Processing {} new files for lesson ID: {}", files.length, lessonId);
            newDocuments = processLessonFiles(existingLesson, files, request.getDocumentsMetadata());
            if (existingLesson.getDocuments() == null) {
                existingLesson.setDocuments(new ArrayList<>());
            }
            existingLesson.getDocuments().addAll(newDocuments);
        }

        // Step 4: Save updated lesson
        Lesson savedLesson = lessonRepository.save(existingLesson);

        log.info("Successfully updated lesson ID: {} with {} new documents", lessonId, newDocuments.size());
        return mapToLessonResponse(savedLesson);
    }
}