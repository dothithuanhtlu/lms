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
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

@Entity
@Table(name = "subjects")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "SubjectCode mustn't be empty")
    private String subjectCode;

    @Column(nullable = false)
    @NotBlank(message = "SubjectName mustn't be empty")
    private String subjectName;

    @Column(nullable = false)
    @NotBlank(message = "Credits mustn't be empty")
    private Integer credits;

    private String preferredSemesterPattern;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private List<Course> courses = new ArrayList<>();

    @Column(nullable = false)
    @NotBlank(message = "SubjectType mustn't be empty")
    @Pattern(regexp = "GENERAL|SPECIALIZED", message = "SubjectType must be GENERAL or SPECIALIZED")
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;
}
