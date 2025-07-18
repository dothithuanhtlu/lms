package vn.doan.lms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.doan.lms.domain.dto.Meta;
import vn.doan.lms.domain.dto.QuizDTO;
import vn.doan.lms.domain.dto.ResultPaginationDTO;
import vn.doan.lms.service.interfaces.IQuizService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {

    private final IQuizService quizService;

    @GetMapping("/student/course/{courseId}")
    public ResponseEntity<Object> studentGetQuizByCourseId(@PathVariable Long courseId,
                                                            @RequestParam(name = "page", defaultValue = "1") int page,
                                                            @RequestParam(name = "size", defaultValue = "10") int size,
                                                            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
                                                            @RequestParam(name = "sortDirection", defaultValue = "DESC") String sortDirection
                                                            ) {
        Page<QuizDTO> quiz = quizService.studentGetQuizOfCourse(courseId, page-1, size, sortBy, sortDirection);

        return ResponseEntity.ok(buildPaginatedResponse(quiz));
    }

    @GetMapping("/teacher/course/{courseId}")
    public ResponseEntity<Object> teacherGetQuizByCourseId(@PathVariable Long courseId,
                                                           @RequestParam(name = "page", defaultValue = "1") int page,
                                                           @RequestParam(name = "size", defaultValue = "10") int size,
                                                           @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
                                                           @RequestParam(name = "sortDirection", defaultValue = "DESC") String sortDirection
    ) {
        Page<QuizDTO> quiz = quizService.teacherGetQuizOfCourse(courseId, page-1, size, sortBy, sortDirection);
        return ResponseEntity.ok(buildPaginatedResponse(quiz));
    }

    @PostMapping("/create")
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody @Valid QuizDTO quizDTO) {
        QuizDTO createdQuiz = quizService.createQuiz(quizDTO);
        return ResponseEntity.status(201).body(createdQuiz);
    }

    @PutMapping("/update/{idQuiz}")
    public ResponseEntity<QuizDTO> updateQuiz(@PathVariable Long idQuiz, @RequestBody @Valid QuizDTO quizDTO) {
        QuizDTO updatedQuiz = quizService.updateQuiz(idQuiz, quizDTO);
        return ResponseEntity.ok(updatedQuiz);
    }

    @GetMapping("/{idQuiz}")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long idQuiz) {
        QuizDTO quiz = quizService.getQuizById(idQuiz);
        return ResponseEntity.ok(quiz);
    }

    @DeleteMapping("/delete/{idQuiz}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long idQuiz) {
        quizService.deleteQuiz(idQuiz);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Object> buildPaginatedResponse(Page<QuizDTO> quiz) {
        Meta meta = new Meta();
        meta.setPage(quiz.getNumber() + 1);
        meta.setPageSize(quiz.getSize());
        meta.setPages(quiz.getTotalPages());
        meta.setTotal(quiz.getTotalElements());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(quiz.getContent());
        return ResponseEntity.ok(result);
    }
}
