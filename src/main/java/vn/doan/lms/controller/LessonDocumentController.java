package vn.doan.lms.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.LessonDocument;
import vn.doan.lms.domain.dto.FileUploadResponseDTO;
import vn.doan.lms.domain.dto.LessonDocumentDTO;
import vn.doan.lms.service.implements_class.LessonDocumentService;
import vn.doan.lms.util.error.BadRequestExceptionCustom;

@RestController
@RequestMapping("/api/lessons/{lessonId}/documents")
@AllArgsConstructor
@Slf4j
public class LessonDocumentController {

    private final LessonDocumentService lessonDocumentService;

    /**
     * Upload file to lesson
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDTO> uploadFile(
            @PathVariable("lessonId") Long lessonId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "system") String uploadedBy) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(FileUploadResponseDTO.error("File is empty"));
            }

            LessonDocumentDTO document = lessonDocumentService.uploadFileToLesson(
                    lessonId, file, title, description, uploadedBy);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(FileUploadResponseDTO.success(document));

        } catch (BadRequestExceptionCustom e) {
            log.error("Bad request during file upload: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(FileUploadResponseDTO.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FileUploadResponseDTO.error("Internal server error during file upload"));
        }
    }

    /**
     * Get all documents for a lesson
     */
    @GetMapping
    public ResponseEntity<List<LessonDocumentDTO>> getDocuments(@PathVariable("lessonId") Long lessonId) {
        List<LessonDocumentDTO> documents = lessonDocumentService.getDocumentsByLessonId(lessonId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by type
     */
    @GetMapping("/type/{documentType}")
    public ResponseEntity<List<LessonDocumentDTO>> getDocumentsByType(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("documentType") LessonDocument.DocumentType documentType) {

        List<LessonDocumentDTO> documents = lessonDocumentService
                .getDocumentsByLessonIdAndType(lessonId, documentType);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get document by ID
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<LessonDocumentDTO> getDocument(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("documentId") Long documentId) {

        LessonDocumentDTO document = lessonDocumentService.getDocumentById(documentId);
        return ResponseEntity.ok(document);
    }

    /**
     * Delete document
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("documentId") Long documentId) {

        lessonDocumentService.deleteDocument(documentId);
        return ResponseEntity.ok().build();
    }

    /**
     * Update document metadata
     */
    @PutMapping("/{documentId}")
    public ResponseEntity<LessonDocumentDTO> updateDocument(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("documentId") Long documentId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isDownloadable) {

        LessonDocumentDTO updatedDocument = lessonDocumentService
                .updateDocumentMetadata(documentId, title, description, isDownloadable);
        return ResponseEntity.ok(updatedDocument);
    }

    /**
     * Generate optimized URL for document
     */
    @GetMapping("/{documentId}/optimize")
    public ResponseEntity<String> generateOptimizedUrl(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("documentId") Long documentId,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height) {

        String optimizedUrl = lessonDocumentService.generateOptimizedUrl(documentId, width, height);
        return ResponseEntity.ok(optimizedUrl);
    }

    /**
     * Generate video thumbnail
     */
    @GetMapping("/{documentId}/thumbnail")
    public ResponseEntity<String> generateVideoThumbnail(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("documentId") Long documentId,
            @RequestParam(required = false, defaultValue = "0") Integer timeInSeconds) {

        String thumbnailUrl = lessonDocumentService.generateVideoThumbnail(documentId, timeInSeconds);
        return ResponseEntity.ok(thumbnailUrl);
    }

    /**
     * Count documents for lesson
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countDocuments(@PathVariable("lessonId") Long lessonId) {
        long count = lessonDocumentService.countDocumentsByLessonId(lessonId);
        return ResponseEntity.ok(count);
    }

    /**
     * Download document file
     */
    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("documentId") Long documentId) {
        try {
            LessonDocumentDTO document = lessonDocumentService.getDocumentById(documentId);

            // Check if document is downloadable
            if (!document.getIsDownloadable()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            // Read file from path
            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            // Determine content type
            String contentType = document.getMimeType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                    .body(resource);

        } catch (IOException e) {
            log.error("Error downloading document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * View document file in browser
     */
    @GetMapping("/{documentId}/view")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("documentId") Long documentId) {
        try {
            LessonDocumentDTO document = lessonDocumentService.getDocumentById(documentId);

            // Read file from path
            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            // Determine content type
            String contentType = document.getMimeType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                    .body(resource);

        } catch (IOException e) {
            log.error("Error viewing document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Stream video/audio files
     */
    @GetMapping("/{documentId}/stream")
    public ResponseEntity<Resource> streamDocument(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("documentId") Long documentId) {
        try {
            LessonDocumentDTO document = lessonDocumentService.getDocumentById(documentId);
            // Only allow streaming for video and audio files
            if (!"VIDEO".equals(document.getDocumentType()) &&
                    !"AUDIO".equals(document.getDocumentType())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Read file from path
            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getMimeType()))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                    .body(resource);

        } catch (IOException e) {
            log.error("Error streaming document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

/**
 * Additional controller for course-level document operations
 */
@RestController
@RequestMapping("/api/courses/{courseId}/documents")
@AllArgsConstructor
@Slf4j
class CourseDocumentController {

    private final LessonDocumentService lessonDocumentService;

    /**
     * Get all documents for a course
     */
    @GetMapping
    public ResponseEntity<List<LessonDocumentDTO>> getCourseDocuments(@PathVariable("courseId") Long courseId) {
        List<LessonDocumentDTO> documents = lessonDocumentService.getDocumentsByCourseId(courseId);
        return ResponseEntity.ok(documents);
    }
}
