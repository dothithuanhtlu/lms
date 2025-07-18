package vn.doan.lms.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateQuestionRequestDTO {

    private List<QuestionRequestDTO> questionRequestDTOs;

    private List<Long> deletedQuestionIds;
}
