package vn.doan.lms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.doan.lms.domain.dto.QuestionRequestDTO;
import vn.doan.lms.domain.dto.UpdateQuestionRequestDTO;
import vn.doan.lms.service.implements_class.QuestionService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/create/{idQuiz}")
    public ResponseEntity<Object> createQuestion(@PathVariable Long idQuiz, @RequestBody @Valid List<QuestionRequestDTO> questionRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.createQuestion(idQuiz, questionRequestDTO));
    }

    @PutMapping("/update/{idQuiz}")
    public ResponseEntity<Object> updateQuestion(@PathVariable Long idQuiz, @RequestBody @Valid UpdateQuestionRequestDTO updateQuestionRequestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.updateQuestion(idQuiz, updateQuestionRequestDTO));
    }

    @GetMapping("/teacher/quiz/{idQuiz}")
    public ResponseEntity<Object> teacherGetQuestionByQuiz(@PathVariable Long idQuiz) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.teacherGetQuestionsByQuiz(idQuiz));
    }
    @GetMapping("/student/quiz/{idQuiz}")
    public ResponseEntity<Object> studentGetQuestionByQuiz(@PathVariable Long idQuiz) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.studentGetQuestionsByQuiz(idQuiz));
    }

}
