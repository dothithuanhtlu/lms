package vn.doan.lms.service.interfaces;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import vn.doan.lms.domain.dto.AssignmentCommentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentCommentDTO;
import vn.doan.lms.domain.dto.AssignmentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentDTO;
import vn.doan.lms.domain.dto.AssignmentUpdateDTO;
import vn.doan.lms.domain.dto.CreateAssignmentWithFilesRequest;
import vn.doan.lms.domain.dto.ResultPaginationDTO;

public interface IAssignmentService {

    ResultPaginationDTO getAssignmentsByCourseId(Long courseId, int currentOptional, int pageSizeOptional,
            String keyword, Boolean isPublished, LocalDate startDate, LocalDate endDate, Boolean isLateSubmission);

    AssignmentDTO createAssignment(AssignmentCreateDTO createDTO) throws IOException;

    // New method for creating assignment with files in single request
    AssignmentDTO createAssignmentWithFiles(CreateAssignmentWithFilesRequest request, MultipartFile[] files)
            throws IOException;

    AssignmentDTO updateAssignment(Long assignmentId, AssignmentUpdateDTO updateDTO);

    void deleteAssignment(Long assignmentId);

    AssignmentDTO getAssignmentById(Long assignmentId);

    long countAssignmentsByCourse(Long courseId);

    long countPublishedAssignmentsByCourse(Long courseId, Boolean isPublished);

    AssignmentCommentDTO createAssignmentComment(Long assignmentId, AssignmentCommentCreateDTO commentDTO);

    List<AssignmentCommentDTO> getCommentsByAssignmentId(Long assignmentId);
}
