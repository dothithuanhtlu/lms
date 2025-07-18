package vn.doan.lms.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Question content mustn't be empty")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @NotNull(message = "Quiz mustn't be null")
    private Quiz quiz;

    @NotNull(message = "Points mustn't be null")
    @DecimalMin(value = "0.1", message = "Points must be greater than 0")
    private Double points;

    @NotNull(message = "Order mustn't be null")
    private Integer orderIndex;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();
}