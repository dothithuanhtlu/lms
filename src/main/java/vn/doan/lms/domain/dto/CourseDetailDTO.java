package vn.doan.lms.domain.dto;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.Enrollment;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseDetailDTO {
    private CourseDTO courseInfo;
    private List<EnrollmentDTO> enrollments;
    private Double averageMidtermScore;
    private Double averageFinalScore;
    private Integer completedCount;

    public CourseDetailDTO(Course course, List<Enrollment> enrollments) {
        this.courseInfo = new CourseDTO(course);
        this.enrollments = enrollments.stream()
                .map(EnrollmentDTO::new)
                .collect(Collectors.toList());

        // Tính toán thống kê
        this.averageMidtermScore = calculateAverageMidterm(enrollments);
        this.averageFinalScore = calculateAverageFinal(enrollments);
        this.completedCount = countCompleted(enrollments);
    }

    private Double calculateAverageMidterm(List<Enrollment> enrollments) {
        return enrollments.stream()
                .filter(e -> e.getMidtermScore() != null)
                .mapToDouble(Enrollment::getMidtermScore)
                .average()
                .orElse(0.0);
    }

    private Double calculateAverageFinal(List<Enrollment> enrollments) {
        return enrollments.stream()
                .filter(e -> e.getFinalScore() != null)
                .mapToDouble(Enrollment::getFinalScore)
                .average()
                .orElse(0.0);
    }

    private Integer countCompleted(List<Enrollment> enrollments) {
        return (int) enrollments.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()))
                .count();
    }
}
