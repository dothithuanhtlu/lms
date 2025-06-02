package vn.doan.lms.service.implements_class;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Course;
import vn.doan.lms.domain.Semester;
import vn.doan.lms.domain.dto.CourseDTO;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.repository.SubjectRepository;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    public List<CourseDTO> getCoursesBySubjectId(long subjectId) {
        if (!courseRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("No courses found for subject with id: " + subjectId);
        }
        return courseRepository.findById(subjectId).stream()
                .map(CourseDTO::new)
                .collect(Collectors.toList());

    }

    public List<String> getSemesterBySubjectIdCourse(long subjectId) {
        if (!this.subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
        }
        return this.courseRepository.findAllBySubjectId(subjectId)
                .stream()
                .map(Course::getSemester)
                .filter(Objects::nonNull)
                .map(Semester::getId)
                .distinct()
                .toList();
    }
}
