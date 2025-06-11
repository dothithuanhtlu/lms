package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonCreateWithFilesDTO {
    
    // Lesson basic info
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    private String content;
    
    @NotNull(message = "Lesson order is required") 
    private Integer lessonOrder;
    
    @NotNull(message = "Duration is required")
    private Integer durationMinutes;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @Builder.Default
    private Boolean isPublished = false;
    
    // Files to upload
    private List<MultipartFile> files;
    
    // File metadata
    private List<String> fileDescriptions;  // Description cho từng file
    private List<Integer> fileDisplayOrders; // Display order cho từng file
    private List<Boolean> fileIsPublic;     // Public status cho từng file
}
