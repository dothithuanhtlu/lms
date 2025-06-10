package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.LessonDocument;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDocumentDTO {
    private Long id;
    private String title;
    private String fileName;
    private String filePath;
    private String documentType; // VIDEO, PDF, DOC, DOCX, PPT, PPTX, IMAGE, AUDIO, OTHER
    private Long fileSize;
    private String mimeType;
    private String description;
    private Boolean isDownloadable;
    private LocalDateTime createdAt;
    private String uploadedBy;
    private Long lessonId;
    private String lessonTitle;

    public LessonDocumentDTO(LessonDocument document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.fileName = document.getFileName();
        this.filePath = document.getFilePath();
        this.documentType = document.getDocumentType().name();
        this.fileSize = document.getFileSize();
        this.mimeType = document.getMimeType();
        this.description = document.getDescription();
        this.isDownloadable = document.getIsDownloadable();
        this.createdAt = document.getCreatedAt();
        this.uploadedBy = document.getUploadedBy();
        this.lessonId = document.getLesson().getId();
        this.lessonTitle = document.getLesson().getTitle();
    }
}
