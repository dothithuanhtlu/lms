package vn.doan.lms.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCreateDTO {
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublished;
    private long courseId;
    private List<MultipartFile> files;

}
