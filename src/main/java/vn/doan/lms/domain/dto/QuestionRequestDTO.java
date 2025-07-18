package vn.doan.lms.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequestDTO {

    private Long id;

    @NotBlank(message = "Question content mustn't be empty")
    private String content;

    @NotNull(message = "Points mustn't be null")
    @DecimalMin(value = "0.1", message = "Points must be greater than 0")
    private Double points;

    @NotNull(message = "Order mustn't be null")
    private Integer orderIndex;

    @Valid
    private List<AnswerRequestDTO> answers;

    private List<Long> deletedAnswerIds;
}
