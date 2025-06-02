package vn.doan.lms.domain.dto;

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
    private long id;

    @NotBlank(message = "courseCode mustn't be empty")
    private String courseCode; // VD: "CS101.2024.1.01"

    @NotBlank(message = "semester mustn't be empty")
    private String semester; // VD: "2024-1"

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
        this.semester = course.getSemester();
        this.teacherName = course.getTeacher().getFullName();
        this.teacherCode = course.getTeacher().getUserCode();
        this.maxStudents = course.getMaxStudents();
        this.currentStudents = course.getCurrentStudents();
    }
}
