package vn.doan.lms.service.implements_class;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.repository.SemesterRepository;
import vn.doan.lms.repository.SubjectRepository;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class SemesterService {
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;

}
