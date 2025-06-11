package vn.doan.lms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import vn.doan.lms.domain.dto.LessonCreateDTO;
import vn.doan.lms.domain.dto.LessonCreateWithFilesDTO;
import vn.doan.lms.domain.dto.LessonDTO;
import vn.doan.lms.service.implements_class.LessonService;

@RestController
@RequestMapping("/api/lessons")
@AllArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonDTO>> getLessonsByCourse(@PathVariable("courseId") Long courseId) {
        List<LessonDTO> lessons = lessonService.getLessonsByCourseId(courseId);
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable("lessonId") Long lessonId) {
        LessonDTO lesson = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(lesson);
    }

    @PostMapping
    public ResponseEntity<LessonDTO> createLesson(@Valid @RequestBody LessonCreateDTO createDTO) {
        LessonDTO createdLesson = lessonService.createLesson(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);
    }

    @PostMapping(value = "/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LessonDTO> createLessonWithFiles(
            @RequestParam("title") String title,
            @RequestParam("courseId") Long courseId,
            @RequestParam("lessonOrder") Integer lessonOrder,
            @RequestParam("durationMinutes") Integer durationMinutes,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "isPublished", defaultValue = "false") Boolean isPublished,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "fileDescriptions", required = false) List<String> fileDescriptions,
            @RequestParam(value = "fileDisplayOrders", required = false) List<Integer> fileDisplayOrders,
            @RequestParam(value = "fileIsPublic", required = false) List<Boolean> fileIsPublic,
            Authentication authentication) {
        try {
            // Tạo DTO từ parameters
            LessonCreateWithFilesDTO createDTO = LessonCreateWithFilesDTO.builder()
                    .title(title)
                    .description(description)
                    .content(content)
                    .lessonOrder(lessonOrder)
                    .durationMinutes(durationMinutes)
                    .courseId(courseId)
                    .isPublished(isPublished)
                    .files(files)
                    .fileDescriptions(fileDescriptions)
                    .fileDisplayOrders(fileDisplayOrders)
                    .fileIsPublic(fileIsPublic)
                    .build();

            // Handle authentication - if null, use default username
            String username = (authentication != null && authentication.getName() != null)
                    ? authentication.getName()
                    : "admin"; // Default username when authentication is disabled

            LessonDTO createdLesson = lessonService.createLessonWithFiles(createDTO, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLesson);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create lesson with files: " + e.getMessage(), e);
        }
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
