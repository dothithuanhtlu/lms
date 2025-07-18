package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.doan.lms.domain.StudentAnswer;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentAnswerResponseDTO {

    private long id;

    private QuestionResponseDTO question;

    private AnswerResponseDTO selectedAnswer;

    private Double pointsEarned;

    public static StudentAnswerResponseDTO mapToDTO(StudentAnswer studentAnswer) {
        if (studentAnswer == null) {
            return null;
        }
        return StudentAnswerResponseDTO.builder()
                .id(studentAnswer.getId())
                .question(QuestionResponseDTO.mapToDTO(studentAnswer.getQuestion()))
                .selectedAnswer(AnswerResponseDTO.mapToDTO(studentAnswer.getSelectedAnswer()))
                .pointsEarned(studentAnswer.getPointsEarned())
                .build();
    }
}
