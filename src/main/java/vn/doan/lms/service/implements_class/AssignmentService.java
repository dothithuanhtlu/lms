package vn.doan.lms.service.implements_class;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Assignment;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.dto.AssignmentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentDTO;
import vn.doan.lms.repository.AssignmentRepository;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.service.interfaces.IAssignmentService;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
@Transactional
public class AssignmentService implements IAssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    @Override
    public List<AssignmentDTO> getAssignmentsByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return assignmentRepository.findByCourseId(courseId)
                .stream()
                .map(AssignmentDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDTO> getPublishedAssignmentsByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return assignmentRepository.findByCourseIdAndIsPublishedTrue(courseId)
                .stream()
                .map(AssignmentDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public AssignmentDTO createAssignment(AssignmentCreateDTO createDTO) {
        Course course = courseRepository.findById(createDTO.getCourseId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Course not found with id: " + createDTO.getCourseId()));

        Assignment assignment = Assignment.builder()
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .instructions(createDTO.getInstructions())
                .assignmentType(Assignment.AssignmentType.valueOf(createDTO.getAssignmentType()))
                .course(course)
                .dueDate(createDTO.getDueDate())
                .maxScore(createDTO.getMaxScore())
                .timeLimitMinutes(createDTO.getTimeLimitMinutes())
                .allowLateSubmission(
                        createDTO.getAllowLateSubmission() != null ? createDTO.getAllowLateSubmission() : false)
                .isPublished(createDTO.getIsPublished() != null ? createDTO.getIsPublished() : false)
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        return new AssignmentDTO(savedAssignment);
    }

    @Override
    public AssignmentDTO updateAssignment(Long assignmentId, AssignmentCreateDTO updateDTO) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        assignment.setTitle(updateDTO.getTitle());
        assignment.setDescription(updateDTO.getDescription());
        assignment.setInstructions(updateDTO.getInstructions());
        assignment.setAssignmentType(Assignment.AssignmentType.valueOf(updateDTO.getAssignmentType()));
        assignment.setDueDate(updateDTO.getDueDate());
        assignment.setMaxScore(updateDTO.getMaxScore());
        assignment.setTimeLimitMinutes(updateDTO.getTimeLimitMinutes());

        if (updateDTO.getAllowLateSubmission() != null) {
            assignment.setAllowLateSubmission(updateDTO.getAllowLateSubmission());
        }

        if (updateDTO.getIsPublished() != null) {
            assignment.setIsPublished(updateDTO.getIsPublished());
        }

        Assignment savedAssignment = assignmentRepository.save(assignment);
        return new AssignmentDTO(savedAssignment);
    }

    @Override
    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + assignmentId);
        }
        assignmentRepository.deleteById(assignmentId);
    }

    @Override
    public AssignmentDTO getAssignmentById(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        return new AssignmentDTO(assignment);
    }

    @Override
    public void publishAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        assignment.setIsPublished(true);
        assignmentRepository.save(assignment);
    }

    @Override
    public void unpublishAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        assignment.setIsPublished(false);
        assignmentRepository.save(assignment);
    }

    @Override
    public List<AssignmentDTO> getAssignmentsByType(Long courseId, Assignment.AssignmentType type) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return assignmentRepository.findByCourseIdAndAssignmentType(courseId, type)
                .stream()
                .map(AssignmentDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public long countAssignmentsByCourse(Long courseId) {
        return assignmentRepository.countByCourseId(courseId);
    }

    @Override
    public long countPublishedAssignmentsByCourse(Long courseId) {
        return assignmentRepository.countByCourseIdAndIsPublishedTrue(courseId);
    }
}
