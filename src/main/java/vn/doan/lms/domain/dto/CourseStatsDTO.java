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
public class CourseStatsDTO {
    private Long courseId;
    private Integer currentStudents;
    private Integer totalLessons;
    private Integer publishedLessons;
    private Integer totalAssignments;
    private Integer publishedAssignments;
    private Double completionRate; // Percentage of published vs total lessons
}
