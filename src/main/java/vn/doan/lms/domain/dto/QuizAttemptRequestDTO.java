package vn.doan.lms.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuizAttemptRequestDTO {

    private Long quizId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private List<StudentAnswerRequestDTO> answers;
}
