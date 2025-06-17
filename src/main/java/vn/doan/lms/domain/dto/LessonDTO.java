package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.validator.constraints.pl.NIP;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private List<LessonDocumentCreateDTO> documents;
    private List<AssignmentDTO> assignments;
    private Integer totalDocuments;
    private Integer totalAssignments;
}
