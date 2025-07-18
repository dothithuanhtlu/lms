package vn.doan.lms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.doan.lms.domain.Question;

@Data
public class AnswerRequestDTO {

    private Long id;

    @NotBlank(message = "Answer content mustn't be empty")
    private String content;

    private Boolean isCorrect;

}
