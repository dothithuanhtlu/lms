package vn.doan.lms.service.implements_class;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.doan.lms.domain.Answer;
import vn.doan.lms.domain.Question;
import vn.doan.lms.domain.Quiz;
import vn.doan.lms.domain.dto.AnswerRequestDTO;
import vn.doan.lms.domain.dto.QuestionRequestDTO;
import vn.doan.lms.domain.dto.QuestionResponseDTO;
import vn.doan.lms.domain.dto.UpdateQuestionRequestDTO;
import vn.doan.lms.repository.AnswerRepository;
import vn.doan.lms.repository.QuestionRepository;
import vn.doan.lms.repository.QuizRepository;
import vn.doan.lms.service.interfaces.IQuestionService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService implements IQuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    @Override
    @Transactional
    public List<QuestionResponseDTO> createQuestion(Long idQuiz, List<QuestionRequestDTO> questionRequestDTO) {
        Quiz quiz = quizRepository.findById(idQuiz).orElseThrow(()-> new EntityNotFoundException("Quiz not found"));
        List<Question> questions = new ArrayList<>();
        for (QuestionRequestDTO questionRequest : questionRequestDTO) {
            Question question = Question.builder()
                    .quiz(quiz)
                    .content(questionRequest.getContent())
                    .orderIndex(questionRequest.getOrderIndex())
                    .points(questionRequest.getPoints())
                    .build();
            List<Answer> answers = new ArrayList<>();
            for (AnswerRequestDTO answerRequest : questionRequest.getAnswers()) {
                Answer answer = Answer.builder()
                        .content(answerRequest.getContent())
                        .isCorrect(answerRequest.getIsCorrect())
                        .question(question)
                        .build();
                answers.add(answer);
            }
            question.setAnswers(answers);
            questions.add(question);
        }
        List<Question> savedQuestions = questionRepository.saveAll(questions);
        return savedQuestions.stream()
                .map(QuestionResponseDTO::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public List<QuestionResponseDTO> updateQuestion(Long idQuiz, UpdateQuestionRequestDTO updateQuestionRequestDTO) {
        Quiz quiz = quizRepository.findById(idQuiz).orElseThrow(()-> new EntityNotFoundException("Quiz not found"));
        List<Question> questions = new ArrayList<>();
        for (QuestionRequestDTO questionRequest : updateQuestionRequestDTO.getQuestionRequestDTOs()) {
            Question existingQuestion;
            if (questionRequest.getId() != null) {
                existingQuestion = questionRepository.findById(questionRequest.getId())
                        .orElse(new Question());
            } else {
                existingQuestion = new Question();
            }
            existingQuestion.setQuiz(quiz);
            existingQuestion.setContent(questionRequest.getContent());
            existingQuestion.setOrderIndex(questionRequest.getOrderIndex());
            existingQuestion.setPoints(questionRequest.getPoints());

            List<Answer> answers = new ArrayList<>();
            for (AnswerRequestDTO answerRequest : questionRequest.getAnswers()) {
                Answer answer;
                if (answerRequest.getId() != null) {
                    answer = answerRepository.findById(answerRequest.getId())
                            .orElse(new Answer());
                } else {
                    answer = new Answer();
                }
                answer.setContent(answerRequest.getContent());
                answer.setIsCorrect(answerRequest.getIsCorrect());
                answer.setQuestion(existingQuestion);
                answers.add(answer);
            }
            if(questionRequest.getDeletedAnswerIds()!= null && !questionRequest.getDeletedAnswerIds().isEmpty()) {
                answerRepository.deleteAllById(questionRequest.getDeletedAnswerIds());
            }
            existingQuestion.setAnswers(answers);
            questions.add(existingQuestion);
        }
        if(updateQuestionRequestDTO.getDeletedQuestionIds() != null && !updateQuestionRequestDTO.getDeletedQuestionIds().isEmpty()) {
            questionRepository.deleteAllById(updateQuestionRequestDTO.getDeletedQuestionIds());
        }
        List<Question> savedQuestions = questionRepository.saveAll(questions);
        return savedQuestions.stream()
                .map(QuestionResponseDTO::mapToDTO)
                .toList();
    }

    @Override
    public List<QuestionResponseDTO> teacherGetQuestionsByQuiz(Long idQuiz) {
        List<Question> questions = questionRepository.findAllByQuizIdOrderByOrderIndexAsc(idQuiz);
        return questions.stream()
                .map(QuestionResponseDTO::mapToDTO)
                .toList();
    }

    @Override
    public List<QuestionResponseDTO> studentGetQuestionsByQuiz(Long idQuiz) {
        List<Question> questions = questionRepository.findAllByQuizIdOrderByOrderIndexAsc(idQuiz);
        return questions.stream()
                .map(QuestionResponseDTO::mapToStudentDTO)
                .toList();
    }
}
