package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for submission statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionStatistics {

    // Basic counts
    private Long totalSubmissions;
    private Long gradedSubmissions;
    private Long ungradedSubmissions;
    private Long lateSubmissions;

    // Score statistics
    private Float averageScore;
    private Float highestScore;
    private Float lowestScore;
    private Float assignmentMaxScore;

    // Percentages
    private Double submissionRate; // percentage of students who submitted
    private Double gradingRate; // percentage of submissions that are graded
    private Double lateRate; // percentage of submissions that are late

    // Additional info
    private Long totalStudentsInCourse;
    private Long studentsNotSubmitted;

    // Grade distribution (optional)
    private Long excellentGrades; // >= 90% of max score
    private Long goodGrades; // 80-89% of max score
    private Long averageGrades; // 70-79% of max score
    private Long belowAverageGrades; // < 70% of max score
}
