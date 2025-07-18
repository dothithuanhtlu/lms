package vn.doan.lms.service.interfaces;


import org.springframework.data.domain.Page;
import vn.doan.lms.domain.dto.QuizDTO;

public interface IQuizService {

    QuizDTO createQuiz(QuizDTO quizDTO);
    QuizDTO updateQuiz(Long idQuiz, QuizDTO quizDTO);
    QuizDTO getQuizById(Long id);
    void deleteQuiz(Long id);
    Page<QuizDTO> teacherGetQuizOfCourse(Long courseId, int page, int size, String sortBy, String sortDir);
    Page<QuizDTO> studentGetQuizOfCourse(Long courseId, int page, int size, String sortBy, String sortDir);
}
