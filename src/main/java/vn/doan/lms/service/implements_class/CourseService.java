package vn.doan.lms.service.implements_class;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.dto.CourseDTO;
import vn.doan.lms.repository.CourseRepository;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public List<CourseDTO> getCoursesBySubjectId(long subjectId) {
        if (!courseRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("No courses found for subject with id: " + subjectId);
        }
        return courseRepository.findById(subjectId).stream()
                .map(CourseDTO::new)
                .collect(Collectors.toList());

    }
}
