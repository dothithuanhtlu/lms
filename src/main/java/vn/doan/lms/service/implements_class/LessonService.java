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

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.Lesson;
import vn.doan.lms.domain.LessonDocument;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.CreateLessonWithFilesRequest;
import vn.doan.lms.domain.dto.DocumentMetadata;
import vn.doan.lms.domain.dto.LessonResponse;
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
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + request.getCourseId()));

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
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + id));

        return mapToLessonResponse(lesson);
    }

    // ✨ Update lesson using ModelMapper
    public LessonResponse updateLesson(Long id, CreateLessonWithFilesRequest request) {
        Lesson existingLesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + id));

        // Map request to existing entity (skip null values)
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(request, existingLesson);

        // Update timestamp
        existingLesson.setUpdatedAt(LocalDateTime.now());

        // Save and return
        lessonRepository.save(existingLesson);
        return mapToLessonResponse(existingLesson);
    }
}
