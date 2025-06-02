package vn.doan.lms.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "courseCode mustn't be empty")
    private String courseCode; // VD: "CS101.2024.1.01"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    @NotBlank(message = "subject mustn't be empty")
    private Subject subject;

    @Column(nullable = false)
    @NotBlank(message = "semester mustn't be empty")
    private String semester; // VD: "2024-1"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    @NotBlank(message = "teacher mustn't be empty")
    private User teacher; // Quan hệ 1-N: 1 course chỉ có 1 teacher

    @NotNull(message = "MaxStudents mustn't be empty")
    private Integer maxStudents;
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer currentStudents;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<Enrollment> enrollments = new ArrayList<>();
}