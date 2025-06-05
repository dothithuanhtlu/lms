package vn.doan.lms.service.implements_class;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.ClassRoom;
import vn.doan.lms.domain.Major;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.ClassRoomDetailDTO;
import vn.doan.lms.domain.dto.ClassRoomListDTO;
import vn.doan.lms.repository.ClassRoomRepository;

@AllArgsConstructor
@Service
public class ClassRoomService {
    private final ClassRoomRepository classRoomRepository;

    public List<String> getAllClassName() {
        return this.classRoomRepository.findAll()
                .stream()
                .map(ClassRoom::getClassName)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassRoomListDTO> getAllClassRooms() {
        return classRoomRepository.findAll().stream()
                .map(this::convertToClassRoomListDTO)
                .collect(Collectors.toList());
    }

    private ClassRoomListDTO convertToClassRoomListDTO(ClassRoom classRoom) {
        User advisor = classRoom.getAdvisor();
        Major major = classRoom.getMajor();

        return ClassRoomListDTO.builder()
                .className(classRoom.getClassName())
                .description(classRoom.getDescription())
                .maxStudents(classRoom.getMaxStudents())
                .currentStudents(classRoom.getCurrentStudents())
                .advisorName(advisor != null ? advisor.getFullName() : null)
                .advisorUserCode(advisor != null ? advisor.getUserCode() : null)
                .majorCode(major != null ? major.getMajorCode() : null)
                .majorName(major != null ? major.getMajorName() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public ClassRoomDetailDTO getClassRoomDetail(long classId) {
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class room not found"));

        return ClassRoomDetailDTO.builder()
                .id(classRoom.getId())
                .className(classRoom.getClassName())
                .description(classRoom.getDescription())
                .maxStudents(classRoom.getMaxStudents())
                .currentStudents(classRoom.getCurrentStudents())
                .advisor(convertToUserDTO(classRoom.getAdvisor()))
                .major(convertToMajorDTO(classRoom.getMajor()))
                .students(classRoom.getUsers().stream()
                        .map(this::convertToUserDTO)
                        .toList())
                .build();
    }

    private ClassRoomDetailDTO.UserDTO convertToUserDTO(User user) {
        if (user == null)
            return null;
        return ClassRoomDetailDTO.UserDTO.builder()
                .id(user.getId())
                .userCode(user.getUserCode())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .phone(user.getPhone())
                .build();
    }

    private ClassRoomDetailDTO.MajorDTO convertToMajorDTO(Major major) {
        if (major == null)
            return null;
        return ClassRoomDetailDTO.MajorDTO.builder()
                .id(major.getId())
                .majorName(major.getMajorName())
                .description(major.getDescription())
                .build();
    }
}
