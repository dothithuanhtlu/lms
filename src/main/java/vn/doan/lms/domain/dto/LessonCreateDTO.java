package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonCreateDTO {
    private String title;
    private String description;
    private String content;
    private Integer lessonOrder;
    private Integer durationMinutes;
    private Boolean isPublished;
    private Long courseId;
}
