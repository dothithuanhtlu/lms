package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.doan.lms.domain.Lesson;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class LessonDocumentCreateDTO {

    private String filePath;

    private String fileName;

    private DocumentType documentType;

    private Boolean isDownloadable;

    private LocalDateTime createdAt;

    private long lessonId;

    public enum DocumentType {
        VIDEO, PDF, DOC, DOCX, PPT, PPTX, IMAGE, AUDIO, OTHER
    }
}
