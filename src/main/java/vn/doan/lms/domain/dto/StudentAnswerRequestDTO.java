package vn.doan.lms.domain.dto;

import lombok.Data;

@Data
public class StudentAnswerRequestDTO {

    private Long questionId;

    private Long selectedAnswerId;

    private Double pointsEarned;
}
