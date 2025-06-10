package vn.doan.lms.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.Question;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDTO {
    private Long id;
    private String questionText;
    private String questionType; // MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, ESSAY, FILL_IN_BLANK
    private Integer questionOrder;
    private Float points;
    private String explanation;
    private String correctAnswer;
    private Long assignmentId;
    private String assignmentTitle;
    private List<QuestionOptionDTO> options;

    public QuestionDTO(Question question) {
        this.id = question.getId();
        this.questionText = question.getQuestionText();
        this.questionType = question.getQuestionType().name();
        this.questionOrder = question.getQuestionOrder();
        this.points = question.getPoints();
        this.explanation = question.getExplanation();
        this.correctAnswer = question.getCorrectAnswer();
        this.assignmentId = question.getAssignment().getId();
        this.assignmentTitle = question.getAssignment().getTitle();
    }
}
