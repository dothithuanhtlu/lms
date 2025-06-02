package vn.doan.lms.service.implements_class;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.repository.MajorRepository;
import vn.doan.lms.util.error.ResourceNotFoundException;

@Service
@AllArgsConstructor
public class MajorService {
    private final MajorRepository majorRepository;

    private boolean isExistsMajorId(long majorId) {
        if (!majorRepository.existsById(majorId)) {
            throw new ResourceNotFoundException("Major not found with id: " + majorId);
        }
        return true;
    }

    public String getMajorNameByMajorId(long majorId) {
        isExistsMajorId(majorId);
        return this.majorRepository.findById(majorId).get().getMajorName();
    }
}
