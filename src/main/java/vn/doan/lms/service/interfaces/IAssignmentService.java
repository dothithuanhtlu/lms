package vn.doan.lms.service.interfaces;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import vn.doan.lms.domain.Assignment;
import vn.doan.lms.domain.dto.AssignmentCommentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentCommentDTO;
import vn.doan.lms.domain.dto.AssignmentCreateDTO;
import vn.doan.lms.domain.dto.AssignmentDTO;
import vn.doan.lms.domain.dto.AssignmentUpdateDTO;
import vn.doan.lms.domain.dto.ResultPaginationDTO;
// import vn.doan.lms.domain.dto.AssignmentCreateDTO;
// import vn.doan.lms.domain.dto.AssignmentDTO;

public interface IAssignmentService {

     ResultPaginationDTO getAssignmentsByCourseId(Long courseId, int currentOptional, int pageSizeOptional, String keyword, Boolean isPublished, LocalDate startDate, LocalDate endDate, Boolean isLateSubmission);

     AssignmentDTO createAssignment(AssignmentCreateDTO createDTO) throws IOException;

     AssignmentDTO updateAssignment(Long assignmentId, AssignmentUpdateDTO updateDTO);

     void deleteAssignment(Long assignmentId);

     AssignmentDTO getAssignmentById(Long assignmentId);

     long countAssignmentsByCourse(Long courseId);

     long countPublishedAssignmentsByCourse(Long courseId, Boolean isPublished);

     AssignmentCommentDTO createAssignmentComment(Long assignmentId, AssignmentCommentCreateDTO commentDTO);

     List<AssignmentCommentDTO> getCommentsByAssignmentId(Long assignmentId);
}
