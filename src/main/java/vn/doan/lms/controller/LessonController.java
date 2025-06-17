package vn.doan.lms.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.Lesson;
import vn.doan.lms.domain.dto.CreateLessonWithFilesRequest;
import vn.doan.lms.domain.dto.LessonCreateDTO;
import vn.doan.lms.domain.dto.LessonResponse;
import vn.doan.lms.service.implements_class.LessonService;
import vn.doan.lms.util.error.BadRequestExceptionCustom;

@RestController
@RequestMapping("/api/lessons")
@AllArgsConstructor
@Slf4j
public class LessonController {

    private final LessonService lessonService;

    private void validateRequest(CreateLessonWithFilesRequest request, MultipartFile[] files) {
        if (files != null && files.length > 10) {
            throw new IllegalArgumentException("Too many files. Maximum 10 files allowed per lesson.");
        }

        if (files != null) {
            for (MultipartFile file : files) {
                if (file.getSize() > 100 * 1024 * 1024) { // 100MB
                    throw new IllegalArgumentException(
                            String.format("File '%s' is too large. Maximum 100MB allowed.",
                                    file.getOriginalFilename()));
                }
            }
        }
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createLessonWithFiles(
            @Valid @ModelAttribute CreateLessonWithFilesRequest request,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            log.info("Creating lesson request from user: dothithuanhtlu");

            // Validation
            validateRequest(request, files);

            LessonResponse response = lessonService.createLessonWithFiles(request, files);

            return ResponseEntity.ok(
                    response);

        } catch (EntityNotFoundException e) {
            log.error("Course not found: {}", e.getMessage());
            throw new BadRequestExceptionCustom(e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error creating lesson: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create lesson: " + e.getMessage());
        }
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable("lessonId") Long lessonId) {
        LessonResponse response = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable("lessonId") Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok().build();
    }

    // @GetMapping("/course/{courseId}")
    // public ResponseEntity<List<LessonDTO>>
    // getLessonsByCourse(@PathVariable("courseId") Long courseId) {
    // List<LessonDTO> lessons = lessonService.getLessonsByCourseId(courseId);
    // return ResponseEntity.ok(lessons);
    // }

    // @GetMapping("/{lessonId}")
    // public ResponseEntity<LessonDTO> getLessonById(@PathVariable("lessonId") Long
    // lessonId) {
    // LessonDTO lesson = lessonService.getLessonById(lessonId);
    // return ResponseEntity.ok(lesson);
    // }

    // @PostMapping
    // public ResponseEntity<LessonDTO> createLesson(@Valid @RequestBody
    // LessonCreateDTO createDTO) {
    // LessonDTO createdLesson = lessonService.createLesson(createDTO);
    // return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    // }

    // @PutMapping("/{lessonId}")
    // public ResponseEntity<LessonDTO> updateLesson(
    // @PathVariable Long lessonId,
    // @Valid @RequestBody LessonCreateDTO updateDTO) {
    // LessonDTO updatedLesson = lessonService.updateLesson(lessonId, updateDTO);
    // return ResponseEntity.ok(updatedLesson);
    // }

    // @DeleteMapping("/{lessonId}")
    // public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
    // lessonService.deleteLesson(lessonId);
    // return ResponseEntity.ok().build();
    // }

    // @PutMapping("/{lessonId}/publish")
    // public ResponseEntity<Void> publishLesson(@PathVariable Long lessonId) {
    // lessonService.publishLesson(lessonId);
    // return ResponseEntity.ok().build();
    // }

    // @PutMapping("/{lessonId}/unpublish")
    // public ResponseEntity<Void> unpublishLesson(@PathVariable Long lessonId) {
    // lessonService.unpublishLesson(lessonId);
    // return ResponseEntity.ok().build();
    // }

    // @PutMapping("/course/{courseId}/reorder")
    // public ResponseEntity<Void> reorderLessons(
    // @PathVariable Long courseId,
    // @RequestParam List<Long> lessonIds) {
    // lessonService.reorderLessons(courseId, lessonIds);
    // return ResponseEntity.ok().build();
    // }
}
