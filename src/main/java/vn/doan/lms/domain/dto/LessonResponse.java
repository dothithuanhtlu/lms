package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.doan.lms.domain.LessonDocument;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResponse {
    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long courseId;
    private String courseName;

    private List<DocumentResponse> documents = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentResponse {
        private Long id;
        private String fileName;
        private String filePath;
        private String fileUrl; // Cloudinary URL
        private LessonDocument.DocumentType documentType;
        private Boolean isDownloadable;
        private LocalDateTime createdAt;
    }
}