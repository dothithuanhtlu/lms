package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.Assignment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
    private Long id;
    private String title;
    private String description;
    private Float maxScore;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublished;
    private Boolean allowLateSubmission;
    private Long courseId;
    private String courseCode;
    private String createdByName;
    private List<AssignmentDocumentDTO> documents;
    public AssignmentDTO(Assignment assignment) {
        this.id = assignment.getId();
        this.title = assignment.getTitle();
        this.description = assignment.getDescription();
        this.maxScore = assignment.getMaxScore();
        this.dueDate = assignment.getDueDate();
        this.createdAt = assignment.getCreatedAt();
        this.updatedAt = assignment.getUpdatedAt();
        this.isPublished = assignment.getIsPublished();
        this.allowLateSubmission = assignment.getAllowLateSubmission();
        if (assignment.getCourse() != null) {
            this.courseId = assignment.getCourse().getId();
            this.courseCode = assignment.getCourse().getCourseCode();
        }
        this.createdByName = assignment.getCreatedBy();
        if (assignment.getDocuments() != null && !assignment.getDocuments().isEmpty()) {
            this.documents = assignment.getDocuments()
                .stream()
                .map(doc -> new AssignmentDocumentDTO(doc.getId(), doc.getFilePath(), doc.getFileNameOriginal()))
                .toList();
        }
    }
}
