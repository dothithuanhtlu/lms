package vn.doan.lms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.doan.lms.domain.dto.Meta;
import vn.doan.lms.domain.dto.QuizAttemptRequestDTO;
import vn.doan.lms.domain.dto.QuizAttemptResponseDTO;
import vn.doan.lms.domain.dto.ResultPaginationDTO;
import vn.doan.lms.service.implements_class.QuizAttemptService;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @PostMapping("/submit")
    public ResponseEntity<Object> submitQuizAttempt(@RequestBody QuizAttemptRequestDTO quizAttemptResponseDTO) {
        QuizAttemptResponseDTO quizAttempt = quizAttemptService.submitQuizAttempt(quizAttemptResponseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizAttempt);
    }

    @GetMapping("/teacher/{quizId}")
    public ResponseEntity<Object> teacherGetQuizAttempt(@PathVariable Long quizId,
                                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                                        @RequestParam(name = "size", defaultValue = "10") int size
                                                        ) {
        Page<QuizAttemptResponseDTO> quizAttempts = quizAttemptService.teacherGetQuizAttempt(quizId, page-1, size);
        return  ResponseEntity.status(HttpStatus.OK).body(buildPaginatedResponse(quizAttempts));
    }

    @GetMapping("/student/{courseId}")
    public ResponseEntity<Object> studentGetQuizAttemptByCourse(@PathVariable Long courseId,
                                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                                        @RequestParam(name = "size", defaultValue = "10") int size)
    {
        Page<QuizAttemptResponseDTO> quizAttempts = quizAttemptService.studentGetQuizAttempt(courseId, page-1, size);
        return  ResponseEntity.status(HttpStatus.OK).body(buildPaginatedResponse(quizAttempts));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Object> getQuizAttemptById(@PathVariable Long id) {
        QuizAttemptResponseDTO quizAttempt = quizAttemptService.getQuizAttemptById(id);
        return ResponseEntity.ok(quizAttempt);
    }
    private ResponseEntity<Object> buildPaginatedResponse(Page<QuizAttemptResponseDTO> quizAttemptResponseDTOS) {
        Meta meta = new Meta();
        meta.setPage(quizAttemptResponseDTOS.getNumber() + 1);
        meta.setPageSize(quizAttemptResponseDTOS.getSize());
        meta.setPages(quizAttemptResponseDTOS.getTotalPages());
        meta.setTotal(quizAttemptResponseDTOS.getTotalElements());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(quizAttemptResponseDTOS.getContent());
        return ResponseEntity.ok(result);
    }
}
