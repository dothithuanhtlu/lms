package vn.doan.lms.service.implements_class;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.doan.lms.domain.Answer;
import vn.doan.lms.domain.Question;
import vn.doan.lms.domain.Quiz;
import vn.doan.lms.domain.QuizAttempt;
import vn.doan.lms.domain.StudentAnswer;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.QuizAttemptRequestDTO;
import vn.doan.lms.domain.dto.QuizAttemptResponseDTO;
import vn.doan.lms.domain.dto.StudentAnswerRequestDTO;
import vn.doan.lms.repository.AnswerRepository;
import vn.doan.lms.repository.QuestionRepository;
import vn.doan.lms.repository.QuizAttemptRepository;
import vn.doan.lms.repository.QuizRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.service.interfaces.IQuizAttemptService;
import vn.doan.lms.util.SecurityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.modelmapper.Converters.Collection.map;

@Service
@RequiredArgsConstructor
public class QuizAttemptService implements IQuizAttemptService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Override
    @Transactional
    public QuizAttemptResponseDTO submitQuizAttempt(QuizAttemptRequestDTO quizAttemptRequestDTO) {
        Quiz quiz = quizRepository.findById(quizAttemptRequestDTO.getQuizId()).orElseThrow(() -> new EntityNotFoundException("Quiz not found with id: " + quizAttemptRequestDTO.getQuizId()));
        Optional<String> userCode = SecurityUtil.getCurrentUserLogin();
        if (userCode.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        User student = userRepository.findOneByUserCode(userCode.get());
        QuizAttempt quizAttempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .startTime(quizAttemptRequestDTO.getStartTime())
                .endTime(quizAttemptRequestDTO.getEndTime())
                .build();
        List<StudentAnswer> studentAnswers = new ArrayList<>();
        for(StudentAnswerRequestDTO studentAnswerRequestDTO : quizAttemptRequestDTO.getAnswers()) {
            StudentAnswer studentAnswer = new StudentAnswer();
            Question question = questionRepository.findById(studentAnswerRequestDTO.getQuestionId()).orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + studentAnswerRequestDTO.getQuestionId()));
            Answer answer = answerRepository.findById(studentAnswerRequestDTO.getSelectedAnswerId())
                    .orElseThrow(() -> new EntityNotFoundException("Answer not found with id: " + studentAnswerRequestDTO.getSelectedAnswerId()));
            if(answer.getIsCorrect()){
                studentAnswer.setPointsEarned(question.getPoints());
            }else{
                studentAnswer.setPointsEarned(0.0);
            }
            studentAnswer.setQuestion(question);
            studentAnswer.setSelectedAnswer(answer);
            studentAnswer.setAttempt(quizAttempt);
            studentAnswers.add(studentAnswer);
        }
        quizAttempt.setStudentAnswers(studentAnswers);
        return QuizAttemptResponseDTO.mapToDTO(quizAttemptRepository.save(quizAttempt));
    }

    @Override
    public Page<QuizAttemptResponseDTO> teacherGetQuizAttempt(Long quizId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return quizAttemptRepository.findAllByQuizId(quizId, pageable)
                .map(QuizAttemptResponseDTO::mapToDTO);
    }

    @Override
    public Page<QuizAttemptResponseDTO> studentGetQuizAttempt(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return quizAttemptRepository.getQuizAttemptByUserAndCourseId(courseId, SecurityUtil.getCurrentUserLogin().orElse(null), pageable)
                .map(QuizAttemptResponseDTO::mapToDTO);
    }

    @Override
    public QuizAttemptResponseDTO getQuizAttemptById(Long quizAttemptId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz attempt not found with id: " + quizAttemptId));
        return QuizAttemptResponseDTO.mapToDTO(quizAttempt);
    }
}
