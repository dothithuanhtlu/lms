package vn.doan.lms.domain.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.Course;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    private static final String COMPLEED_COURSE = "Completed";
    private static final String UPCOMING_COURSE = "Upcoming";
    private static final String ACTIVE_COURSE = "Active";
    private long id;

    @NotBlank(message = "courseCode mustn't be empty")
    private String courseCode; // VD: "CS101.2024.1.01"

    @NotBlank(message = "teacherName mustn't be empty")
    private String teacherName; // Quan hệ 1-N: 1 course chỉ có 1 teacher

    @NotBlank(message = "teacherCode mustn't be empty")
    private String teacherCode; // Mã giáo viên

    @NotNull(message = "MaxStudents mustn't be empty")
    private Integer maxStudents;

    private Integer currentStudents;

    public CourseDTO(Course course) {
        this.id = course.getId();
        this.courseCode = course.getCourseCode();
        this.teacherName = course.getTeacher().getFullName();
        this.teacherCode = course.getTeacher().getUserCode();
        this.maxStudents = course.getMaxStudents();
        this.currentStudents = course.getEnrollments() != null ? course.getEnrollments().size() : 0;
        this.startDate = course.getStartDate();
        this.endDate = course.getEndDate();
        if (course.getEndDate() != null && course.getEndDate().isBefore(LocalDate.now())) {
            this.status = COMPLEED_COURSE;
        } else if (course.getStartDate() != null && course.getStartDate().isAfter(LocalDate.now())) {
            this.status = UPCOMING_COURSE;
        } else {
            this.status = ACTIVE_COURSE;
        }
    }

    @NotNull(message = "StartDate mustn't be null")
    private LocalDate startDate;

    @NotNull(message = "EndDate mustn't be null")
    private LocalDate endDate;
    private String status;
}
