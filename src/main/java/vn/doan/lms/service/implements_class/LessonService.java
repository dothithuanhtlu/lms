package vn.doan.lms.service.implements_class;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.Lesson;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.FileUploadRequestDTO;
import vn.doan.lms.domain.dto.LessonCreateDTO;
import vn.doan.lms.domain.dto.LessonCreateWithFilesDTO;
import vn.doan.lms.domain.dto.LessonDTO;
import vn.doan.lms.domain.dto.LessonDocumentDTO;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.LessonRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.service.interfaces.ILessonService;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class LessonService implements ILessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Autowired
    private LessonDocumentService lessonDocumentService;

    @Override
    public List<LessonDTO> getLessonsByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return lessonRepository.findByCourseIdOrderByLessonOrderAsc(courseId)
                .stream()
                .map(LessonDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public LessonDTO createLesson(LessonCreateDTO createDTO) {
        Course course = courseRepository.findById(createDTO.getCourseId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Course not found with id: " + createDTO.getCourseId()));

        // Get next order index
        Integer maxOrder = lessonRepository.findMaxLessonOrderByCourseId(createDTO.getCourseId());
        int nextOrder = (maxOrder != null) ? maxOrder + 1 : 1;

        Lesson lesson = Lesson.builder()
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .content(createDTO.getContent())
                .course(course)
                .lessonOrder(nextOrder)
                .durationMinutes(createDTO.getDurationMinutes())
                .isPublished(createDTO.getIsPublished() != null ? createDTO.getIsPublished() : false)
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);
        return new LessonDTO(savedLesson);
    }

    @Override
    public LessonDTO updateLesson(Long lessonId, LessonCreateDTO updateDTO) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        lesson.setTitle(updateDTO.getTitle());
        lesson.setDescription(updateDTO.getDescription());
        lesson.setContent(updateDTO.getContent());
        lesson.setDurationMinutes(updateDTO.getDurationMinutes());

        if (updateDTO.getIsPublished() != null) {
            lesson.setIsPublished(updateDTO.getIsPublished());
        }

        Lesson savedLesson = lessonRepository.save(lesson);
        return new LessonDTO(savedLesson);
    }

    @Override
    public void deleteLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }
        lessonRepository.deleteById(lessonId);
    }

    @Override
    public LessonDTO getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findByIdWithDocuments(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
        return new LessonDTO(lesson);
    }

    @Override
    public void publishLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
        lesson.setIsPublished(true);
        lessonRepository.save(lesson);
    }

    @Override
    public void unpublishLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
        lesson.setIsPublished(false);
        lessonRepository.save(lesson);
    }

    @Override
    public void reorderLessons(Long courseId, List<Long> lessonIds) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        for (int i = 0; i < lessonIds.size(); i++) {
            Long lessonId = lessonIds.get(i);
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

            if (lesson.getCourse().getId() != courseId) {
                throw new IllegalArgumentException("Lesson " + lessonId + " does not belong to course " + courseId);
            }

            lesson.setLessonOrder(i + 1);
            lessonRepository.save(lesson);
        }
    }

    @Override
    public List<LessonDTO> getPublishedLessonsByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return lessonRepository.findByCourseIdAndIsPublishedTrueOrderByLessonOrderAsc(courseId)
                .stream()
                .map(LessonDTO::new).collect(Collectors.toList());
    }

    @Override
    public long countLessonsByCourse(Long courseId) {
        return lessonRepository.countByCourseId(courseId);
    }

    @Transactional
    public LessonDTO createLessonWithFiles(LessonCreateWithFilesDTO createDTO, String username) {
        // 1. Validate course exists and user has permission
        Course course = courseRepository.findById(createDTO.getCourseId())
                .orElseThrow(() -> new BadRequestExceptionCustom("Course not found"));

        User teacher = null;

        // If username is "admin" (when authentication is disabled), use the course's
        // teacher
        if ("admin".equals(username)) {
            teacher = course.getTeacher();
        } else {
            teacher = userRepository.findOneByUserCode(username);
            if (teacher == null) {
                throw new BadRequestExceptionCustom("Teacher not found");
            }

            // Check if teacher owns this course
            if (!Objects.equals(course.getTeacher().getId(), teacher.getId())) {
                throw new BadRequestExceptionCustom("You don't have permission to add lessons to this course");
            }
        }

        // 2. Check lesson order uniqueness
        boolean orderExists = lessonRepository.existsByCourseIdAndLessonOrder(
                createDTO.getCourseId(), createDTO.getLessonOrder());
        if (orderExists) {
            throw new BadRequestExceptionCustom(
                    "Lesson order " + createDTO.getLessonOrder() + " already exists in this course");
        }

        // 3. Create lesson first
        Lesson lesson = Lesson.builder()
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .content(createDTO.getContent())
                .lessonOrder(createDTO.getLessonOrder())
                .durationMinutes(createDTO.getDurationMinutes())
                .isPublished(createDTO.getIsPublished())
                .course(course)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);

        // 4. Upload files if provided
        List<LessonDocumentDTO> uploadedDocuments = new ArrayList<>();
        if (createDTO.getFiles() != null && !createDTO.getFiles().isEmpty()) {
            uploadedDocuments = uploadFilesToLesson(savedLesson.getId(), createDTO, teacher);
        }

        // 5. Create response DTO
        LessonDTO lessonDTO = new LessonDTO(savedLesson);
        lessonDTO.setDocuments(uploadedDocuments); // Set uploaded documents

        log.info("Created lesson with {} documents for course {}",
                uploadedDocuments.size(), createDTO.getCourseId());

        return lessonDTO;
    }

    private List<LessonDocumentDTO> uploadFilesToLesson(Long lessonId, LessonCreateWithFilesDTO createDTO,
            User uploader) {
        List<LessonDocumentDTO> uploadedDocuments = new ArrayList<>();
        List<MultipartFile> files = createDTO.getFiles();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            if (file.isEmpty()) {
                continue;
            }

            try {
                // Prepare upload request
                FileUploadRequestDTO uploadRequest = FileUploadRequestDTO.builder()
                        .displayOrder(getFileDisplayOrder(createDTO.getFileDisplayOrders(), i))
                        .description(getFileDescription(createDTO.getFileDescriptions(), i))
                        .isPublic(getFileIsPublic(createDTO.getFileIsPublic(), i))
                        .build();
                // Upload file
                LessonDocumentDTO document = lessonDocumentService.uploadFileToLesson(
                        lessonId, file, file.getOriginalFilename(),
                        uploadRequest.getDescription(), uploader.getUserCode());

                uploadedDocuments.add(document);

            } catch (Exception e) {
                log.error("Failed to upload file {} for lesson {}: {}",
                        file.getOriginalFilename(), lessonId, e.getMessage());
                // Continue with other files, don't fail entire operation
            }
        }

        return uploadedDocuments;
    }

    // Helper methods để xử lý optional parameters
    private Integer getFileDisplayOrder(List<Integer> orders, int index) {
        if (orders != null && index < orders.size() && orders.get(index) != null) {
            return orders.get(index);
        }
        return index + 1; // Default display order
    }

    private String getFileDescription(List<String> descriptions, int index) {
        if (descriptions != null && index < descriptions.size()) {
            return descriptions.get(index);
        }
        return null;
    }

    private Boolean getFileIsPublic(List<Boolean> publicFlags, int index) {
        if (publicFlags != null && index < publicFlags.size() && publicFlags.get(index) != null) {
            return publicFlags.get(index);
        }
        return true; // Default to public
    }
}
