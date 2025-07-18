package vn.doan.lms.service.implements_class;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.Quiz;
import vn.doan.lms.domain.dto.QuizDTO;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.QuizRepository;
import vn.doan.lms.service.interfaces.IQuizService;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class QuizService implements IQuizService {

    private final CourseRepository courseRepository;
    private final QuizRepository quizRepository;

    @Override
    public QuizDTO createQuiz(QuizDTO quizDTO) {

        Course course = courseRepository.findById(quizDTO.getCourseId()).orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Quiz quiz = Quiz.builder()
            .title(quizDTO.getTitle())
            .description(quizDTO.getDescription())
            .course(course)
            .durationMinutes(quizDTO.getDurationMinutes())
            .isActive(quizDTO.getIsActive())
            .startTime(quizDTO.getStartTime())
            .endTime(quizDTO.getEndTime())
            .build();
        Quiz savedQuiz = quizRepository.save(quiz);

        return QuizDTO.mapToDTO(savedQuiz);
    }

    @Override
    public QuizDTO updateQuiz(Long idQuiz, QuizDTO quizDTO) {
        Quiz existingQuiz = quizRepository.findById(idQuiz).orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        Course course = null;
        if(existingQuiz.getCourse().getId() != quizDTO.getCourseId()) {
            course = courseRepository.findById(quizDTO.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            existingQuiz.setCourse(course);
        }
        existingQuiz.setTitle(quizDTO.getTitle());
        existingQuiz.setDescription(quizDTO.getDescription());
        existingQuiz.setDurationMinutes(quizDTO.getDurationMinutes());
        existingQuiz.setStartTime(quizDTO.getStartTime());
        existingQuiz.setEndTime(quizDTO.getEndTime());
        existingQuiz.setIsActive(quizDTO.getIsActive());
        Quiz updatedQuiz = quizRepository.save(existingQuiz);
        return QuizDTO.mapToDTO(updatedQuiz);
    }

    @Override
    public QuizDTO getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        return QuizDTO.mapToDTO(quiz);
    }

    @Override
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        quizRepository.delete(quiz);
    }

    @Override
    public Page<QuizDTO> teacherGetQuizOfCourse(Long courseId, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Quiz> quizDTOPage = quizRepository.findAllByCourseId(courseId, pageable);
        return quizDTOPage.map(QuizDTO::mapToDTO);
    }

    @Override
    public Page<QuizDTO> studentGetQuizOfCourse(Long courseId, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Quiz> quizDTOPage = quizRepository.studentGetByCourseId(courseId, pageable);
        return quizDTOPage.map(QuizDTO::mapToDTO);
    }
}
