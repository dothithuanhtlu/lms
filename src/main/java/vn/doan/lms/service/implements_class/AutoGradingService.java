package vn.doan.lms.service.implements_class;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.Assignment;
import vn.doan.lms.domain.Submission;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.Submission.SubmissionStatus;
import vn.doan.lms.repository.AssignmentRepository;
import vn.doan.lms.repository.SubmissionRepository;
import vn.doan.lms.repository.UserRepository;

@Service
@AllArgsConstructor
@Slf4j
public class AutoGradingService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    /**
     * Scheduled task to auto-grade overdue assignments
     * Runs every 30 minutes to check for newly expired assignments
     */
    @Scheduled(fixedRate = 1800000) // Run every 30 minutes (1800000 ms)
    @Transactional
    public void autoGradeExpiredAssignments() {
        log.info("Starting scheduled auto-grading for expired assignments");

        LocalDateTime now = LocalDateTime.now();

        // Find assignments that are expired and don't allow late submission
        List<Assignment> expiredAssignments = assignmentRepository.findExpiredAssignmentsForAutoGrading(now);

        if (expiredAssignments.isEmpty()) {
            log.debug("No expired assignments found for auto-grading");
            return;
        }

        int totalProcessed = 0;
        int totalAssignments = expiredAssignments.size();

        log.info("Found {} expired assignments to process", totalAssignments);

        for (Assignment assignment : expiredAssignments) {
            try {
                int processed = processExpiredAssignment(assignment);
                totalProcessed += processed;

                if (processed > 0) {
                    log.info("Auto-graded {} students for assignment: {} (ID: {})",
                            processed, assignment.getTitle(), assignment.getId());
                } else {
                    log.debug("No students to auto-grade for assignment: {} (ID: {})",
                            assignment.getTitle(), assignment.getId());
                }

            } catch (Exception e) {
                log.error("Error auto-grading assignment ID: {} - {}",
                        assignment.getId(), e.getMessage(), e);
            }
        }

        if (totalProcessed > 0) {
            log.info("Scheduled auto-grading completed. {} students processed across {} assignments",
                    totalProcessed, totalAssignments);
        } else {
            log.info("📋 Scheduled auto-grading completed. No new submissions to process");
        }
    }

    /**
     * Process a single expired assignment
     */
    @Transactional
    public int processExpiredAssignment(Assignment assignment) {
        log.debug("Processing expired assignment: {} (ID: {})", assignment.getTitle(), assignment.getId());

        // Get all students enrolled in the course
        List<User> students = userRepository.findStudentsByCourseId(assignment.getCourse().getId());

        if (students.isEmpty()) {
            log.warn("No students found in course ID: {}", assignment.getCourse().getId());
            return 0;
        }

        int processedCount = 0;

        for (User student : students) {
            // Check if student already has a submission
            Optional<Submission> existingSubmission = submissionRepository
                    .findByAssignmentIdAndStudentId(assignment.getId(), student.getId());

            if (existingSubmission.isEmpty()) {
                // Student hasn't submitted - create zero-grade submission
                log.debug("Creating zero submission for student: {} ({})",
                        student.getFullName(), student.getUserCode());

                Submission zeroSubmission = Submission.builder()
                        .assignment(assignment)
                        .student(student)
                        .submittedAt(assignment.getDueDate()) // Use due date as submission time
                        .gradedAt(LocalDateTime.now())
                        .score(0.0f)
                        .feedback("Automatic grade: Assignment not submitted by due date")
                        .status(SubmissionStatus.SUBMITTED)
                        .isLate(false) // Not late since we're using due date
                        .build();

                submissionRepository.save(zeroSubmission);
                processedCount++;

                log.debug("Zero submission created for student: {} with ID: {}",
                        student.getUserCode(), zeroSubmission.getId());
            } else {
                log.debug("Student {} already has submission - skipping", student.getUserCode());
            }
        }

        return processedCount;
    }

    /**
     * Manual trigger for specific assignment (called by other services when needed)
     */
    @Transactional
    public int checkAndAutoGradeAssignment(Long assignmentId) {
        try {
            Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
            if (assignmentOpt.isEmpty()) {
                log.warn("Assignment not found for auto-grade check: {}", assignmentId);
                return 0;
            }

            Assignment assignment = assignmentOpt.get();

            // Check if assignment is eligible for auto-grading
            if (!isEligibleForAutoGrading(assignment)) {
                log.debug("Assignment ID: {} is not eligible for auto-grading", assignmentId);
                return 0;
            }

            int processed = processExpiredAssignment(assignment);
            if (processed > 0) {
                log.info("Manual auto-grade: {} students processed for assignment ID: {}",
                        processed, assignmentId);
            }

            return processed;

        } catch (Exception e) {
            log.error("Error in manual auto-grade check for assignment {}: {}", assignmentId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Check if assignment is eligible for auto-grading
     */
    private boolean isEligibleForAutoGrading(Assignment assignment) {
        LocalDateTime now = LocalDateTime.now();

        // Must have due date
        if (assignment.getDueDate() == null) {
            return false;
        }

        // Must be overdue
        if (!assignment.getDueDate().isBefore(now)) {
            return false;
        }

        // Must not allow late submission
        if (assignment.getAllowLateSubmission() != null && assignment.getAllowLateSubmission()) {
            return false;
        }

        // Must be published
        if (assignment.getIsPublished() == null || !assignment.getIsPublished()) {
            return false;
        }

        return true;
    }

    /**
     * Get auto-grading statistics (for monitoring)
     */
    public void logAutoGradingStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Assignment> eligibleAssignments = assignmentRepository.findExpiredAssignmentsForAutoGrading(now);

        log.info("Auto-grading status: {} assignments eligible for auto-grading", eligibleAssignments.size());

        for (Assignment assignment : eligibleAssignments) {
            List<User> students = userRepository.findStudentsByCourseId(assignment.getCourse().getId());
            List<Submission> submissions = submissionRepository.findByAssignmentId(assignment.getId());

            int pendingCount = students.size() - submissions.size();

            if (pendingCount > 0) {
                log.info("⏰ Assignment: {} - {} students pending auto-grade",
                        assignment.getTitle(), pendingCount);
            }
        }
    }
}
