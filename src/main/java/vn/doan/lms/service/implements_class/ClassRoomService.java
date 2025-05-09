package vn.doan.lms.service.implements_class;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.ClassRoom;
import vn.doan.lms.repository.ClassRoomRepository;

@AllArgsConstructor
@Service
public class ClassRoomService {
    private final ClassRoomRepository classRoomRepository;

    public List<String> getAllClassName() {
        return this.classRoomRepository.findAll()
                .stream()
                .map(ClassRoom::getClassName) // assuming ClassRoom has a getClassName method
                .collect(Collectors.toList());
    }
}
