package vn.doan.lms.service.interfaces;

import java.util.List;

import vn.doan.lms.domain.dto.CourseCreateDTO;
import vn.doan.lms.domain.dto.CourseDTO;
import vn.doan.lms.domain.dto.CourseDTOInfo;
import vn.doan.lms.domain.dto.CourseDetailDTO;
import vn.doan.lms.domain.dto.CourseFullDetailDTO;
import vn.doan.lms.domain.dto.StudentCourseDetailDTO;

/**
 * Service interface for course-related operations
 */
public interface ICourseService {

    /**
     * Create a new course
     * 
     * @param createDTO Course creation data
     * @return Created course DTO
     */
    CourseDTO createCourse(CourseCreateDTO createDTO);

    /**
     * Get information about courses
     * 
     * @return Course info DTO
     */
    CourseDTOInfo getInforCourses();

    /**
     * Get courses by subject ID
     * 
     * @param subjectId Subject ID
     * @return List of course DTOs
     */
    List<CourseDTO> getCoursesBySubjectId(long subjectId);

    /**
     * Get course details
     * 
     * @param courseId Course ID
     * @return Course detail DTO
     */
    CourseDetailDTO getCourseDetails(Long courseId);

    /**
     * Delete course by ID
     * 
     * @param courseId Course ID
     */
    void deleteCourseById(Long courseId);

    /**
     * Update course
     * 
     * @param courseDTO Course data
     * @param courseId  Course ID
     */
    void updateCourse(CourseDTO courseDTO, long courseId);

    /**
     * Get current student count for a course
     * 
     * @param courseId Course ID
     * @return Current student count
     */
    int getCurrentStudentCount(Long courseId);

    /**
     * Get full course details with all assignments and student submissions (for
     * admin/teacher view)
     * 
     * @param courseId Course ID
     * @return Course full detail DTO
     */
    CourseFullDetailDTO getCourseFullDetails(Long courseId);

    /**
     * Get courses by teacher ID
     * 
     * @param teacherId Teacher ID
     * @return List of course DTOs
     */
    List<CourseDTO> getCoursesByTeacherId(Long teacherId);

    /**
     * Get courses by student ID
     * 
     * @param studentId Student ID
     * @return List of course DTOs
     */
    List<CourseDTO> getCoursesByStudentId(Long studentId);

    /**
     * Get student-specific course details
     * Returns course information with the student's submission status for each
     * assignment
     * 
     * @param courseId        Course ID
     * @param studentUsername Student's username
     * @return Student course detail DTO
     */
    StudentCourseDetailDTO getStudentCourseDetails(Long courseId, String studentUsername);
}
