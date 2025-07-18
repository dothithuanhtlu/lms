package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.doan.lms.domain.QuizAttempt;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizAttemptResponseDTO {

    private Long id;

    private QuizDTO quiz;

    private UserDTO student;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private List<StudentAnswerResponseDTO> studentAnswers;

    private Double totalScore;

    public static QuizAttemptResponseDTO mapToDTO(QuizAttempt quizAttempt) {
        if (quizAttempt == null) {
            return null;
        }
        double totalScore = quizAttempt.getStudentAnswers().stream()
                .mapToDouble(studentAnswer -> studentAnswer.getPointsEarned() != null ? studentAnswer.getPointsEarned() : 0.0)
                .sum();

        return QuizAttemptResponseDTO.builder()
                .id(quizAttempt.getId())
                .quiz(QuizDTO.mapToDTO(quizAttempt.getQuiz()))
                .student(UserDTO.mapToDTO(quizAttempt.getStudent()))
                .startTime(quizAttempt.getStartTime())
                .endTime(quizAttempt.getEndTime())
                .studentAnswers(quizAttempt.getStudentAnswers().stream()
                        .map(StudentAnswerResponseDTO::mapToDTO)
                        .toList())
                .totalScore(totalScore)
                .build();
    }
}
