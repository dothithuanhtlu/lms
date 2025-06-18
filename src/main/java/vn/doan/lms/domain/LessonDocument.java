package vn.doan.lms.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lesson_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(nullable = false, length = 500)
    @NotBlank(message = "File path mustn't be empty")
    private String filePath;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "File name mustn't be empty")
    private String fileName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Document type mustn't be null")
    private DocumentType documentType;

    @Column(name = "is_downloadable")
    @Builder.Default
    private Boolean isDownloadable = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @NotNull(message = "Lesson mustn't be null")
    private Lesson lesson;

    public enum DocumentType {
        VIDEO("video"),
        PDF("raw"),
        DOC("raw"),
        DOCX("raw"),
        PPT("raw"),
        PPTX("raw"),
        IMAGE("image"),
        AUDIO("video"),
        OTHER("raw");

        private final String resourceType;

        DocumentType(String resourceType) {
            this.resourceType = resourceType;
        }

        public String getResourceType() {
            return resourceType;
        }
    }

    public String getResourceType() {
        return this.documentType.getResourceType();
    }
}
