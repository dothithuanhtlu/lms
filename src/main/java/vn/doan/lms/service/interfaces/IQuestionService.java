package vn.doan.lms.service.interfaces;

import vn.doan.lms.domain.dto.QuestionRequestDTO;
import vn.doan.lms.domain.dto.QuestionResponseDTO;
import vn.doan.lms.domain.dto.UpdateQuestionRequestDTO;

import java.util.List;

public interface IQuestionService {
    List<QuestionResponseDTO> createQuestion(Long idQuiz,List<QuestionRequestDTO> questionRequestDTO);
    List<QuestionResponseDTO> updateQuestion(Long idQuiz, UpdateQuestionRequestDTO updateQuestionRequestDTO);
    List<QuestionResponseDTO> teacherGetQuestionsByQuiz(Long idQuiz);
    List<QuestionResponseDTO> studentGetQuestionsByQuiz(Long idQuiz);
}
