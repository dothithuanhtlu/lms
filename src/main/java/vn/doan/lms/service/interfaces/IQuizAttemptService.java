package vn.doan.lms.service.interfaces;

import org.springframework.data.domain.Page;
import vn.doan.lms.domain.dto.QuizAttemptRequestDTO;
import vn.doan.lms.domain.dto.QuizAttemptResponseDTO;

public interface IQuizAttemptService {

    QuizAttemptResponseDTO submitQuizAttempt(QuizAttemptRequestDTO quizAttemptRequestDTO);
    Page<QuizAttemptResponseDTO> teacherGetQuizAttempt(Long quizId, int page, int size);
    Page<QuizAttemptResponseDTO> studentGetQuizAttempt(Long courseId, int page, int size);
    QuizAttemptResponseDTO getQuizAttemptById(Long quizAttemptId);
}
