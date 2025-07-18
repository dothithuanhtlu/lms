package vn.doan.lms.domain.dto;

import lombok.Data;
import vn.doan.lms.domain.Question;

import java.util.List;

@Data
public class QuestionResponseDTO {

    private Long id;

    private String content;

    private Double points;

    private Integer orderIndex;

    private List<AnswerResponseDTO> answers;

    private List<AnswerStudentResponseDTO> answerStudents;

    public static QuestionResponseDTO mapToDTO(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setPoints(question.getPoints());
        dto.setOrderIndex(question.getOrderIndex());
        List<AnswerResponseDTO> answerDTOs = question.getAnswers().stream()
                .map(answer -> {
                    AnswerResponseDTO answerDTO = new AnswerResponseDTO();
                    answerDTO.setId(answer.getId());
                    answerDTO.setContent(answer.getContent());
                    answerDTO.setIsCorrect(answer.getIsCorrect());
                    return answerDTO;
                })
                .toList();
        dto.setAnswers(answerDTOs);
        return dto;
    }
    public static QuestionResponseDTO mapToStudentDTO(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setPoints(question.getPoints());
        dto.setOrderIndex(question.getOrderIndex());
        List<AnswerStudentResponseDTO> answerDTOs = question.getAnswers().stream()
                .map(answer -> {
                    AnswerStudentResponseDTO answerDTO = new AnswerStudentResponseDTO();
                    answerDTO.setId(answer.getId());
                    answerDTO.setContent(answer.getContent());
                    return answerDTO;
                }).toList();
        dto.setAnswerStudents(answerDTOs);
        return dto;
    }
}
