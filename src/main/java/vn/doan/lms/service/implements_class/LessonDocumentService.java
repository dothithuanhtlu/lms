package vn.doan.lms.service.implements_class;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.CloudinaryUploadResult;
import vn.doan.lms.domain.Lesson;
import vn.doan.lms.domain.LessonDocument;
import vn.doan.lms.domain.dto.LessonDocumentDTO;
import vn.doan.lms.repository.LessonDocumentRepository;
import vn.doan.lms.repository.LessonRepository;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class LessonDocumentService {

    private final LessonDocumentRepository lessonDocumentRepository;
    private final LessonRepository lessonRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Upload file to lesson
     * 
     * @param lessonId    ID of the lesson
     * @param file        File to upload
     * @param title       Title for the document
     * @param description Description for the document
     * @param uploadedBy  Username of uploader
     * @return Created LessonDocumentDTO
     */
    public LessonDocumentDTO uploadFileToLesson(Long lessonId, MultipartFile file, String title,
            String description, String uploadedBy) {
        // Validate lesson exists
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        // Check if file with same name already exists for this lesson
        if (lessonDocumentRepository.existsByLessonIdAndFileName(lessonId, file.getOriginalFilename())) {
            throw new BadRequestExceptionCustom(
                    "File with name '" + file.getOriginalFilename() + "' already exists for this lesson");
        }

        try {
            // Upload to Cloudinary
            String folder = "lms/lessons/" + lessonId + "/documents";
            CloudinaryUploadResult uploadResult = cloudinaryService.uploadFile(file, folder);

            // Determine document type based on file extension and content type
            LessonDocument.DocumentType documentType = determineDocumentType(file);

            // Create LessonDocument entity
            LessonDocument document = LessonDocument.builder()
                    .title(title != null ? title : file.getOriginalFilename())
                    .fileName(file.getOriginalFilename())
                    .filePath(uploadResult.getSecureUrl())
                    .documentType(documentType)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .description(description)
                    .uploadedBy(uploadedBy)
                    .lesson(lesson)
                    .isDownloadable(true)
                    .build();

            LessonDocument savedDocument = lessonDocumentRepository.save(document);
            log.info("Document uploaded successfully for lesson {}: {}", lessonId, savedDocument.getFileName());

            return new LessonDocumentDTO(savedDocument);

        } catch (Exception e) {
            log.error("Error uploading file to lesson {}: {}", lessonId, e.getMessage());
            throw new BadRequestExceptionCustom("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Get all documents for a lesson
     * 
     * @param lessonId ID of the lesson
     * @return List of LessonDocumentDTO
     */
    @Transactional(readOnly = true)
    public List<LessonDocumentDTO> getDocumentsByLessonId(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }

        return lessonDocumentRepository.findByLessonId(lessonId)
                .stream()
                .map(LessonDocumentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get documents by lesson and type
     * 
     * @param lessonId     ID of the lesson
     * @param documentType Type of document
     * @return List of LessonDocumentDTO
     */
    @Transactional(readOnly = true)
    public List<LessonDocumentDTO> getDocumentsByLessonIdAndType(Long lessonId,
            LessonDocument.DocumentType documentType) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }

        return lessonDocumentRepository.findByLessonIdAndDocumentType(lessonId, documentType)
                .stream()
                .map(LessonDocumentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get document by ID
     * 
     * @param documentId ID of the document
     * @return LessonDocumentDTO
     */
    @Transactional(readOnly = true)
    public LessonDocumentDTO getDocumentById(Long documentId) {
        LessonDocument document = lessonDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        return new LessonDocumentDTO(document);
    }

    /**
     * Delete document
     * 
     * @param documentId ID of the document to delete
     */
    public void deleteDocument(Long documentId) {
        LessonDocument document = lessonDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        try {
            // Extract public ID from Cloudinary URL for deletion
            String publicId = extractPublicIdFromUrl(document.getFilePath());
            String resourceType = getResourceTypeFromDocumentType(document.getDocumentType());

            // Delete from Cloudinary
            boolean deleted = cloudinaryService.deleteFile(publicId, resourceType);
            if (!deleted) {
                log.warn("Failed to delete file from Cloudinary: {}", publicId);
            }

            // Delete from database
            lessonDocumentRepository.delete(document);
            log.info("Document deleted successfully: {}", document.getFileName());

        } catch (Exception e) {
            log.error("Error deleting document {}: {}", documentId, e.getMessage());
            throw new BadRequestExceptionCustom("Failed to delete document: " + e.getMessage());
        }
    }

    /**
     * Update document metadata
     * 
     * @param documentId     ID of the document
     * @param title          New title
     * @param description    New description
     * @param isDownloadable Whether file is downloadable
     * @return Updated LessonDocumentDTO
     */
    public LessonDocumentDTO updateDocumentMetadata(Long documentId, String title, String description,
            Boolean isDownloadable) {
        LessonDocument document = lessonDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        if (title != null)
            document.setTitle(title);
        if (description != null)
            document.setDescription(description);
        if (isDownloadable != null)
            document.setIsDownloadable(isDownloadable);

        LessonDocument updatedDocument = lessonDocumentRepository.save(document);
        return new LessonDocumentDTO(updatedDocument);
    }

    /**
     * Get all documents for a course
     * 
     * @param courseId ID of the course
     * @return List of LessonDocumentDTO
     */
    @Transactional(readOnly = true)
    public List<LessonDocumentDTO> getDocumentsByCourseId(Long courseId) {
        return lessonDocumentRepository.findByCourseId(courseId)
                .stream()
                .map(LessonDocumentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Count documents for a lesson
     * 
     * @param lessonId ID of the lesson
     * @return Number of documents
     */
    @Transactional(readOnly = true)
    public long countDocumentsByLessonId(Long lessonId) {
        return lessonDocumentRepository.countByLessonId(lessonId);
    }

    /**
     * Generate optimized URL for document viewing
     * 
     * @param documentId ID of the document
     * @param width      Optional width for optimization
     * @param height     Optional height for optimization
     * @return Optimized URL
     */
    public String generateOptimizedUrl(Long documentId, Integer width, Integer height) {
        LessonDocument document = lessonDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        String publicId = extractPublicIdFromUrl(document.getFilePath());
        return cloudinaryService.generateOptimizedUrl(publicId, width, height, "auto");
    }

    /**
     * Generate video thumbnail for video documents
     * 
     * @param documentId    ID of the video document
     * @param timeInSeconds Time in seconds for thumbnail
     * @return Thumbnail URL
     */
    public String generateVideoThumbnail(Long documentId, Integer timeInSeconds) {
        LessonDocument document = lessonDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        if (document.getDocumentType() != LessonDocument.DocumentType.VIDEO) {
            throw new BadRequestExceptionCustom("Document is not a video file");
        }

        String publicId = extractPublicIdFromUrl(document.getFilePath());
        return cloudinaryService.generateVideoThumbnail(publicId, timeInSeconds);
    }

    // Helper methods

    /**
     * Determine document type based on file
     * 
     * @param file MultipartFile to analyze
     * @return DocumentType
     */
    private LessonDocument.DocumentType determineDocumentType(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (contentType != null) {
            if (contentType.startsWith("video/"))
                return LessonDocument.DocumentType.VIDEO;
            if (contentType.startsWith("audio/"))
                return LessonDocument.DocumentType.AUDIO;
            if (contentType.startsWith("image/"))
                return LessonDocument.DocumentType.IMAGE;
            if (contentType.equals("application/pdf"))
                return LessonDocument.DocumentType.PDF;
        }

        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            switch (extension) {
                case "pdf":
                    return LessonDocument.DocumentType.PDF;
                case "doc":
                case "docx":
                    return LessonDocument.DocumentType.DOC;
                case "ppt":
                case "pptx":
                    return LessonDocument.DocumentType.PPT;
                case "mp4":
                case "avi":
                case "mov":
                case "wmv":
                case "flv":
                case "webm":
                case "mkv":
                case "m4v":
                    return LessonDocument.DocumentType.VIDEO;
                case "mp3":
                case "wav":
                case "flac":
                case "aac":
                case "ogg":
                    return LessonDocument.DocumentType.AUDIO;
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                case "bmp":
                case "svg":
                case "webp":
                    return LessonDocument.DocumentType.IMAGE;
                default:
                    return LessonDocument.DocumentType.OTHER;
            }
        }

        return LessonDocument.DocumentType.OTHER;
    }

    /**
     * Extract public ID from Cloudinary URL
     * 
     * @param url Cloudinary URL
     * @return Public ID
     */
    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("cloudinary.com")) {
            return null;
        }

        try {
            // URL format:
            // https://res.cloudinary.com/cloud-name/resource_type/upload/version/folder/public_id.format
            // or:
            // https://res.cloudinary.com/cloud-name/resource_type/upload/folder/public_id.format
            String[] parts = url.split("/");

            // Find the upload index
            int uploadIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i])) {
                    uploadIndex = i;
                    break;
                }
            }

            if (uploadIndex >= 0 && uploadIndex + 1 < parts.length) {
                // Get everything after upload, but remove the file extension from the last part
                StringBuilder publicId = new StringBuilder();

                for (int i = uploadIndex + 1; i < parts.length; i++) {
                    if (i > uploadIndex + 1) {
                        publicId.append("/");
                    }

                    String part = parts[i];
                    // If this is the last part, remove file extension
                    if (i == parts.length - 1) {
                        int dotIndex = part.lastIndexOf(".");
                        if (dotIndex > 0) {
                            part = part.substring(0, dotIndex);
                        }
                    }
                    publicId.append(part);
                }

                return publicId.toString();
            }
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", url, e);
        }

        return null;
    }

    /**
     * Get resource type for Cloudinary based on document type
     * 
     * @param documentType Document type
     * @return Resource type string
     */
    private String getResourceTypeFromDocumentType(LessonDocument.DocumentType documentType) {
        switch (documentType) {
            case VIDEO:
                return "video";
            case IMAGE:
                return "image";
            case AUDIO:
                return "video"; // Audio files are handled as video in Cloudinary
            default:
                return "raw";
        }
    }
}
