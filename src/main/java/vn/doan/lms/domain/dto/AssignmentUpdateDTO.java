package vn.doan.lms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentUpdateDTO {
    @NotBlank(message = "Title must not be blank")
    private String title;

    private String description;

    private Float maxScore;

    @NotNull(message = "Due date must not be null")
    private LocalDateTime dueDate;

    private Boolean isPublished;

    private Boolean allowLateSubmission;

    private Long courseId;

    private List<MultipartFile> fileUploadsNew;

    private List<Long> fileDeleteIds;

}