package vn.doan.lms.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.doan.lms.domain.LessonDocument;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentMetadata {

    @NotNull(message = "Document type mustn't be null")
    private LessonDocument.DocumentType documentType;

    @Builder.Default
    private Boolean isDownloadable = true;

    private String description; // Optional description for document
}