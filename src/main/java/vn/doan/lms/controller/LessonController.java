package vn.doan.lms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import vn.doan.lms.domain.dto.LessonCreateDTO;
import vn.doan.lms.domain.dto.LessonDTO;
import vn.doan.lms.service.implements_class.LessonService;

@RestController
@RequestMapping("/api/lessons")
@AllArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonDTO>> getLessonsByCourse(@PathVariable Long courseId) {
        List<LessonDTO> lessons = lessonService.getLessonsByCourseId(courseId);
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable Long lessonId) {
        LessonDTO lesson = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(lesson);
    }

    @PostMapping
    public ResponseEntity<LessonDTO> createLesson(@Valid @RequestBody LessonCreateDTO createDTO) {
        LessonDTO createdLesson = lessonService.createLesson(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<LessonDTO> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonCreateDTO updateDTO) {
        LessonDTO updatedLesson = lessonService.updateLesson(lessonId, updateDTO);
        return ResponseEntity.ok(updatedLesson);
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{lessonId}/publish")
    public ResponseEntity<Void> publishLesson(@PathVariable Long lessonId) {
        lessonService.publishLesson(lessonId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{lessonId}/unpublish")
    public ResponseEntity<Void> unpublishLesson(@PathVariable Long lessonId) {
        lessonService.unpublishLesson(lessonId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/course/{courseId}/reorder")
    public ResponseEntity<Void> reorderLessons(
            @PathVariable Long courseId,
            @RequestParam List<Long> lessonIds) {
        lessonService.reorderLessons(courseId, lessonIds);
        return ResponseEntity.ok().build();
    }
}
