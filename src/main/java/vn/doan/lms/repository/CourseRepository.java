package vn.doan.lms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doan.lms.domain.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllBySubjectId(long subjectId);

    Course findOneById(long courseId);

    long countByEndDateBefore(LocalDate today);

    long countByStartDateAfter(LocalDate today);

    long countByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate start, LocalDate end);
}
