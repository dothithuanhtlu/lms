package vn.doan.lms.domain.dto;

import lombok.Data;
import vn.doan.lms.domain.Answer;

@Data
public class AnswerResponseDTO {

    private Long id;

    private String content;

    private Boolean isCorrect;

    public static  AnswerResponseDTO mapToDTO(Answer answer) {
        if (answer == null) {
            return null;
        }
        AnswerResponseDTO dto = new AnswerResponseDTO();
        dto.setId(answer.getId());
        dto.setContent(answer.getContent());
        dto.setIsCorrect(answer.getIsCorrect());
        return dto;
    }
}
