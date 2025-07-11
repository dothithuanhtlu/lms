package vn.doan.lms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
        List<Course> findAllBySubjectId(long subjectId);

        @Query("SELECT c FROM Course c LEFT JOIN FETCH c.enrollments WHERE c.subject.id = :subjectId")
        List<Course> findAllBySubjectIdWithEnrollments(@Param("subjectId") long subjectId);

        @Query("SELECT c FROM Course c LEFT JOIN FETCH c.enrollments WHERE c.id = :courseId")
        Course findByIdWithEnrollments(@Param("courseId") Long courseId);

        long countByEndDateBefore(LocalDate today);

        long countByStartDateAfter(LocalDate today);

        long countByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate start, LocalDate end);

        @Query("SELECT c FROM Course c " +
                        "LEFT JOIN FETCH c.subject " +
                        "LEFT JOIN FETCH c.teacher " +
                        "LEFT JOIN FETCH c.enrollments e " +
                        "LEFT JOIN FETCH e.student s " +
                        "LEFT JOIN FETCH s.classRoom " +
                        "WHERE c.id = :courseId")
        Course findByIdWithFullDetails(@Param("courseId") Long courseId);

        @Query("SELECT c FROM Course c " +
                        "LEFT JOIN FETCH c.lessons " +
                        "WHERE c.id = :courseId")
        Course findByIdWithLessons(@Param("courseId") Long courseId);

        @Query("SELECT c FROM Course c " +
                        "LEFT JOIN FETCH c.assignments a " +
                        "WHERE c.id = :courseId")
        Course findByIdWithAssignments(@Param("courseId") Long courseId);

        boolean existsByCourseCode(String courseCode);

        List<Course> findByTeacher_Id(Long teacherId);
}
