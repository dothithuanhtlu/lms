package vn.doan.lms.domain.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CourseCreateDTO {

    @NotBlank(message = "Course code must not be empty")
    private String courseCode;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    private LocalDate endDate;

    @NotNull(message = "Max students must not be null")
    @Positive(message = "Max students must be positive")
    private Integer maxStudents;

    @NotNull(message = "Subject ID must not be null")
    private Long subjectId;

    @NotNull(message = "Teacher ID must not be null")
    private Long teacherId;
}
