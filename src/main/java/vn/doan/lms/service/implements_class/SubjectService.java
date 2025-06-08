package vn.doan.lms.service.implements_class;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.Subject;
import vn.doan.lms.domain.dto.MajorRequestDTO;
import vn.doan.lms.domain.dto.SubjectDTO;
import vn.doan.lms.repository.MajorRepository;
import vn.doan.lms.repository.SubjectRepository;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final MajorRepository majorRepository;

    public List<SubjectDTO> getSubjectsByMajorId(long majorId) {
        if (!this.majorRepository.existsById(majorId)) {
            throw new ResourceNotFoundException("Major not found with id: " + majorId);
        }
        List<Subject> subjects = this.subjectRepository.findAllByMajorId(majorId);
        return subjects.isEmpty() ? Collections.emptyList()
                : subjects.stream()
                        .map(SubjectDTO::new)
                        .collect(Collectors.toList());
    }

    public String getSubjectNameBySubjectId(long subjectId) {
        if (!this.subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
        }
        return this.subjectRepository.findById(subjectId).get().getSubjectName();
    }

    public List<SubjectDTO> getSubjectDTOsByMajorId(long majorId) {
        if (!this.majorRepository.existsById(majorId)) {
            throw new ResourceNotFoundException("Major not found with id: " + majorId);
        }
        return this.subjectRepository.findAllByMajorId(majorId)
                .stream()
                .map(SubjectDTO::new)
                .collect(Collectors.toList());
    }

    public SubjectDTO getSubjectBySubjectId(long subjectId) {
        if (!this.subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject not found with id");
        }
        return this.subjectRepository.findById(subjectId)
                .map(SubjectDTO::new).get();
    }

}
