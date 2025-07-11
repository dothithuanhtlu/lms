package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.Lesson;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDTO {
    private Long id;
    private String title;
    private String description;
    private String content;
    private Integer lessonOrder;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublished;
    private Long courseId;
    private String courseCode;
    private List<LessonDocumentDTO> documents;
    private List<AssignmentDTO> assignments;
    private Integer totalDocuments;
    private Integer totalAssignments;

    public LessonDTO(Lesson lesson) {
        this.id = lesson.getId();
        this.title = lesson.getTitle();
        this.description = lesson.getDescription();
        this.content = lesson.getContent();
        this.lessonOrder = lesson.getLessonOrder();
        this.durationMinutes = lesson.getDurationMinutes();
        this.createdAt = lesson.getCreatedAt();
        this.updatedAt = lesson.getUpdatedAt();
        this.isPublished = lesson.getIsPublished();
        this.courseId = lesson.getCourse().getId();
        this.courseCode = lesson.getCourse().getCourseCode();

        // Load documents
        if (lesson.getDocuments() != null && !lesson.getDocuments().isEmpty()) {
            this.documents = lesson.getDocuments().stream()
                    .map(LessonDocumentDTO::new)
                    .collect(java.util.stream.Collectors.toList());
            this.totalDocuments = lesson.getDocuments().size();
        } else {
            this.documents = new java.util.ArrayList<>();
            this.totalDocuments = 0;
        }

        // Load assignments
        if (lesson.getAssignments() != null && !lesson.getAssignments().isEmpty()) {
            this.assignments = lesson.getAssignments().stream()
                    .map(vn.doan.lms.domain.dto.AssignmentDTO::new)
                    .collect(java.util.stream.Collectors.toList());
            this.totalAssignments = lesson.getAssignments().size();
        } else {
            this.assignments = new java.util.ArrayList<>();
            this.totalAssignments = 0;
        }
    }
}
