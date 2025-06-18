package vn.doan.lms.service.implements_class;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Assignment;
import vn.doan.lms.domain.AssignmentComment;
import vn.doan.lms.domain.AssignmentDocument;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.AssignmentCommentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentCommentDTO;
import vn.doan.lms.domain.dto.AssignmentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentDTO;
import vn.doan.lms.domain.dto.AssignmentUpdateDTO;
import vn.doan.lms.domain.dto.Meta;
import vn.doan.lms.domain.dto.ResultPaginationDTO;
import vn.doan.lms.repository.AssignmentCommentRepository;
import vn.doan.lms.repository.AssignmentDocumentRepository;
import vn.doan.lms.repository.AssignmentRepository;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.service.interfaces.IAssignmentService;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
@Transactional
public class AssignmentService implements IAssignmentService {

     private final AssignmentRepository assignmentRepository;
     private final CourseRepository courseRepository;
     private final CloudinaryService cloudinaryService;
     private final AssignmentDocumentRepository assignmentDocumentRepository;
     private final UserRepository userRepository;
     private final AssignmentCommentRepository assignmentCommentRepository;
     @Override
     public ResultPaginationDTO getAssignmentsByCourseId(Long courseId, int currentOptional, int pageSizeOptional, String keyword, Boolean isPublished, LocalDate startDate, LocalDate endDate, Boolean isLateSubmission) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
         Pageable pageable = Pageable.ofSize(pageSizeOptional).withPage(currentOptional);
         Page<AssignmentDTO> pageAssignment = assignmentRepository.filterAssignments(courseId, keyword, isPublished, startDate, endDate, isPublished,pageable)
             .map(AssignmentDTO::new);
         // Tạo metadata phân trang
         Meta meta = new Meta();
         meta.setPage(pageAssignment.getNumber());
         meta.setPageSize(pageAssignment.getSize());
         meta.setPages(pageAssignment.getTotalPages());
         meta.setTotal(pageAssignment.getTotalElements());

         // Trả về ResultPaginationDTO
         ResultPaginationDTO result = new ResultPaginationDTO();
         result.setMeta(meta);
         result.setResult(pageAssignment.get());
         return result;
     }

     @Override
     @Transactional
     public AssignmentDTO createAssignment(AssignmentCreateDTO createDTO) throws IOException {
         String userName = SecurityContextHolder.getContext().getAuthentication().getName();
         Course course = courseRepository.findById(createDTO.getCourseId()).orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + createDTO.getCourseId()));
         Assignment assignment = new Assignment();
         List<Map> fileUploadResults = cloudinaryService.uploadMultipleFiles(createDTO.getFileUploads(), "assignments/" + createDTO.getTitle() + "/");
         List<AssignmentDocument> assignmentDocuments = new ArrayList<>();
         for( Map fileUploadResult : fileUploadResults) {
             String filePath = (String) fileUploadResult.get("secure_url");
             String originalFileName = (String) fileUploadResult.get("name_file_original");

             // Create and save AssignmentDocument entity
             AssignmentDocument assignmentDocument = new AssignmentDocument();
             assignmentDocument.setFileNameOriginal(originalFileName);
             assignmentDocument.setFilePath(filePath);
             assignmentDocument.setAssignment(assignment);
             assignmentDocuments.add(assignmentDocument);
         }

         assignment.setTitle(createDTO.getTitle());
         assignment.setDescription(createDTO.getDescription());
         assignment.setCourse(course);
         assignment.setDueDate(createDTO.getDueDate());
         assignment.setMaxScore(createDTO.getMaxScore());
         assignment.setAllowLateSubmission(createDTO.getAllowLateSubmission() != null ? createDTO.getAllowLateSubmission() : false);
         assignment.setIsPublished(createDTO.getIsPublished() != null ? createDTO.getIsPublished() : false);
         assignment.setDocuments(assignmentDocuments);
         assignment.setCreatedBy(userName);

         Assignment savedAssignment = assignmentRepository.save(assignment);
         return new AssignmentDTO(savedAssignment);
     }

     @Override
     @Transactional
     public AssignmentDTO updateAssignment(Long assignmentId, AssignmentUpdateDTO updateDTO) {
         Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
         if(updateDTO.getCourseId() != null) {
             Course course = courseRepository.findById(updateDTO.getCourseId()).orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + updateDTO.getCourseId()));
             assignment.setCourse(course);
         }
         List<AssignmentDocument> assignmentDocuments = assignment.getDocuments();
         if(updateDTO.getFileUploadsNew() != null) {
             List<Map> fileUploadResults = cloudinaryService.uploadMultipleFiles(updateDTO.getFileUploadsNew(), "assignments/" + updateDTO.getTitle() + "/");
             for( Map fileUploadResult : fileUploadResults) {
                 String filePath = (String) fileUploadResult.get("secure_url");
                 String originalFileName = (String) fileUploadResult.get("name_file_original");

                 // Create and save AssignmentDocument entity
                 AssignmentDocument assignmentDocument = new AssignmentDocument();
                 assignmentDocument.setFileNameOriginal(originalFileName);
                 assignmentDocument.setFilePath(filePath);
                 assignmentDocument.setAssignment(assignment);
                 assignmentDocumentRepository.save(assignmentDocument);
             }
         }
         if (updateDTO.getFileDeleteIds() != null) {
             for(Long documentId : updateDTO.getFileDeleteIds()) {
                 AssignmentDocument document = assignmentDocumentRepository.findById(documentId)
                         .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
                 assignmentDocuments.remove(document);
                 assignmentDocumentRepository.delete(document);
             }
         }

         assignment.setTitle(updateDTO.getTitle());
         assignment.setDescription(updateDTO.getDescription());
         assignment.setDueDate(updateDTO.getDueDate());
         assignment.setMaxScore(updateDTO.getMaxScore());
         assignment.setAllowLateSubmission(updateDTO.getAllowLateSubmission() != null ? updateDTO.getAllowLateSubmission() : false);
         assignment.setIsPublished(updateDTO.getIsPublished() != null ? updateDTO.getIsPublished() : false);
         assignment.setDocuments(assignmentDocuments);

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
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        return new AssignmentDTO(assignment);
     }

    @Override
    public long countAssignmentsByCourse(Long courseId) {
        return assignmentRepository.countByCourseId(courseId);
    }

    @Override
    public long countPublishedAssignmentsByCourse(Long courseId, Boolean isPublished) {
        return assignmentRepository.countByCourseIdAndIsPublished(courseId, isPublished);
    }

    @Override
    public AssignmentCommentDTO createAssignmentComment(Long assignmentId, AssignmentCommentCreateDTO commentDTO) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if(email.equals("anonymousUser")) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + email));
        AssignmentComment comment = new AssignmentComment();
        comment.setContent(commentDTO.getContent());
        comment.setUser(user);
        comment.setAssignment(assignment);
        AssignmentComment savedComment = assignmentCommentRepository.save(comment);
        return new AssignmentCommentDTO(savedComment);
    }

    @Override
    public List<AssignmentCommentDTO> getCommentsByAssignmentId(Long assignmentId) {
        return assignmentCommentRepository.findByAssignmentId(assignmentId)
               .stream()
               .map(AssignmentCommentDTO::new)
               .toList();
    }

}
