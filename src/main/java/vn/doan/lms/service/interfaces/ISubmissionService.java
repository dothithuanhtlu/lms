package vn.doan.lms.service.interfaces;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import vn.doan.lms.domain.dto.GradeSubmissionRequest;
import vn.doan.lms.domain.dto.ResultPaginationDTO;
import vn.doan.lms.domain.dto.SubmissionCreateRequest;
import vn.doan.lms.domain.dto.SubmissionResponse;
import vn.doan.lms.domain.dto.SubmissionStatistics;

/**
 * Interface for Submission Service
 * Handles all submission-related operations including CRUD, grading, and file
 * management
 */
public interface ISubmissionService {

    /**
     * Create a new submission for an assignment
     * 
     * @param request  Submission request containing assignment ID and optional
     *                 files
     * @param username Username of the submitting student
     * @return SubmissionResponse with submission details
     */
    // SubmissionResponse createSubmission(SubmissionCreateRequest request, String
    // username);

    /**
     * Update an existing submission (only if not graded)
     * 
     * @param submissionId ID of the submission to update
     * @param request      Update request with new files or content
     * @param username     Username of the student updating the submission
     * @return SubmissionResponse with updated submission details
     */

    // SubmissionResponse updateSubmission(Long submissionId,
    // SubmissionCreateRequest request, String username);

    /**
     * Grade a submission (teacher only)
     * 
     * @param submissionId ID of the submission to grade
     * @param request      Grade request containing score and feedback
     * @param username     Username of the grading teacher
     * @return SubmissionResponse with graded submission details
     */
    SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request, String username);

    /**
     * Get submission details by ID
     * 
     * @param submissionId ID of the submission
     * @return SubmissionResponse with submission details
     */
    SubmissionResponse getSubmissionById(Long submissionId);

    /**
     * Get all submissions for a specific assignment (teacher view)
     * 
     * @param assignmentId ID of the assignment
     * @param page         Page number for pagination
     * @param size         Page size for pagination
     * @return ResultPaginationDTO with SubmissionResponse for all submissions
     */
    ResultPaginationDTO getSubmissionsByAssignment(Long assignmentId, int page, int size);

    /**
     * Get submissions by student for a specific assignment
     * 
     * @param assignmentId ID of the assignment
     * @param username     Username of the student
     * @return List of SubmissionResponse for student's submissions
     */
    // List<SubmissionResponse> getSubmissionsByStudentAndAssignment(Long
    // assignmentId, String username);

    /**
     * Get submissions by student (with optional course filter)
     * 
     * @param username Username of the student
     * @param courseId Optional course ID to filter by
     * @param page     Page number for pagination
     * @param size     Page size for pagination
     * @return ResultPaginationDTO with SubmissionResponse for student's submissions
     */
    ResultPaginationDTO getSubmissionsByStudent(String username, Long courseId, int page, int size);

    /**
     * Delete a submission (only if not graded)
     * 
     * @param submissionId ID of the submission to delete
     * @param username     Username of the user deleting the submission
     */
    void deleteSubmission(Long submissionId, String username);

    /**
     * Check if a student has submitted for a specific assignment
     * 
     * @param assignmentId ID of the assignment
     * @param username     Username of the student
     * @return true if student has submitted, false otherwise
     */
    boolean hasStudentSubmitted(Long assignmentId, String username);

    /**
     * Get submission statistics for an assignment
     * 
     * @param assignmentId ID of the assignment
     * @return Submission statistics (total, graded, late, etc.)
     */
    SubmissionStatistics getSubmissionStatistics(Long assignmentId);

    /**
     * Submit or update assignment submission (unified method)
     * Automatically determines whether to create new or update existing submission
     * 
     * @param request  Submission data
     * @param files    Files to upload
     * @param username Username of the student
     * @return SubmissionResponse with submission details
     * @throws IOException if file processing fails
     */
    SubmissionResponse submitOrUpdateSubmission(SubmissionCreateRequest request, MultipartFile[] files, String username)
            throws IOException;

    /**
     * Get count of unsubmitted assignments that student can still submit
     * (assignments that are either not due yet OR overdue but allow late
     * submission)
     * 
     * @param studentId ID of the student
     * @return Count of assignments the student can still submit
     */
    long getUnsubmittedAssignmentCountByStudentId(Long studentId);
}
