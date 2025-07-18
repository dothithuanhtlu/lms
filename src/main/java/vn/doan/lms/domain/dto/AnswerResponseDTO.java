package vn.doan.lms.domain.dto;

import lombok.Data;
import vn.doan.lms.domain.Question;

@Data
public class AnswerResponseDTO {

    private Long id;

    private String content;

    private Boolean isCorrect;

}
