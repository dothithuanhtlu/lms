package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.QuestionOption;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOptionDTO {
    private Long id;
    private String optionText;
    private Integer optionOrder;
    private Boolean isCorrect;
    private Long questionId;

    public QuestionOptionDTO(QuestionOption option) {
        this.id = option.getId();
        this.optionText = option.getOptionText();
        this.optionOrder = option.getOptionOrder();
        this.isCorrect = option.getIsCorrect();
        this.questionId = option.getQuestion().getId();
    }
}
