package vn.doan.lms.service.implements_class;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import vn.doan.lms.domain.ClassRoom;
import vn.doan.lms.domain.Role;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.user_dto.StudentDTO;
import vn.doan.lms.domain.dto.user_dto.StudentDTOUpdate;
import vn.doan.lms.repository.ClassRoomRepository;
import vn.doan.lms.repository.RoleRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.util.SecurityUtil;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.EmailValidationException;
import vn.doan.lms.util.error.ResourceNotFoundException;
import vn.doan.lms.util.error.StoredProcedureFailedException;
import vn.doan.lms.util.error.UserCodeValidationException;

@Getter
@Setter
@AllArgsConstructor
@Service
public class UserService {
    private static final String ROLE_STUDENT = "STUDENT";
    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private EntityManager entityManager;
    private final RoleRepository roleRepository;

    public User getUserByUserCode(String userCode) {
        if(!isExistUserCode(userCode)) {
            throw new ResourceNotFoundException("User not found with user code");
        }
        return this.userRepository.findOneByUserCode(userCode);
    }

    private StudentDTO convertToStudentDTO(User user) {
        return StudentDTO.builder()
                .userCode(user.getUserCode())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .address(user.getAddress())
                .phone(user.getPhone())
                .className(user.getClassRoom().getClassName())
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .updatedAt(user.getUpdatedAt())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    public List<StudentDTO> getAllStudents() {
        Role role = this.roleRepository.findOneByNameRole(ROLE_STUDENT);
        if (role != null) {
            return this.userRepository.findAllByRoleId(role.getId()).stream()
                    .map(user -> this.convertToStudentDTO(user))
                    .toList();
        }
        return null;
    }

    private boolean isExistUserCode(String userCode) {
        return this.userRepository.existsByUserCode(userCode);
    }

    private boolean isExistEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public StudentDTOUpdate createStudent(StudentDTOUpdate studentDTOUpdate) {
        if (studentDTOUpdate == null) {
            return null;
        }
        if (isExistUserCode(studentDTOUpdate.getUserCode())) {
            throw new BadRequestExceptionCustom("User code already exists");
        }
        if (isExistEmail(studentDTOUpdate.getEmail())) {
            throw new BadRequestExceptionCustom("Email already exists");
        }

        Role role = this.roleRepository.findOneByNameRole(ROLE_STUDENT);
        ClassRoom classRoom = this.classRoomRepository.findByClassName(studentDTOUpdate.getClassName());
        studentDTOUpdate.setCreatedBy(SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "");
        studentDTOUpdate.setCreatedAt(Instant.now());
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("create_stu")
                .registerStoredProcedureParameter("p_class_id", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_user_code", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_password", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_email", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_full_name", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_address", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_gender", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_phone", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_date_of_birth", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_role_id", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_create_at", Instant.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_create_by", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_Result", Integer.class, ParameterMode.OUT)
                .setParameter("p_class_id", classRoom.getId())
                .setParameter("p_user_code", studentDTOUpdate.getUserCode())
                .setParameter("p_password", studentDTOUpdate.getPassword()) // Nếu có trường này trong DTO
                .setParameter("p_email", studentDTOUpdate.getEmail())
                .setParameter("p_full_name", studentDTOUpdate.getFullName())
                .setParameter("p_address", studentDTOUpdate.getAddress())
                .setParameter("p_gender", studentDTOUpdate.getGender())
                .setParameter("p_phone", studentDTOUpdate.getPhone()) // Nếu có trường này trong DTO
                .setParameter("p_date_of_birth", java.sql.Date.valueOf(studentDTOUpdate.getDateOfBirth()))
                .setParameter("p_role_id", role.getId())
                .setParameter("p_create_at", studentDTOUpdate.getCreatedAt())
                .setParameter("p_create_by", studentDTOUpdate.getCreatedBy());

        query.execute();
        Integer result = (Integer) query.getOutputParameterValue("p_Result");
        if (result == 0) {
            throw new StoredProcedureFailedException("Thêm học sinh thất bại - có thể lớp đã đầy");
        }
        return studentDTOUpdate;
    }

    @Transactional
    public void deleteStudent(String userCode) throws ResourceNotFoundException {
        if (!isExistUserCode(userCode)) {
            throw new ResourceNotFoundException("User code is not exists");
        }
        this.userRepository.deleteUserByUserCode(userCode);
    }

    private StudentDTOUpdate convertToStudentDTOUpdate(User user) {
        return StudentDTOUpdate.builder()
                .userCode(user.getUserCode())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .password(user.getPassword())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .address(user.getAddress())
                .phone(user.getPhone())
                .className(user.getClassRoom().getClassName())
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .updatedAt(user.getUpdatedAt())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    public StudentDTOUpdate getStudentByUserCode(String userCode) {
        if (!isExistUserCode(userCode)) {
            throw new ResourceNotFoundException("User code is not exists");
        }
        return convertToStudentDTOUpdate(this.userRepository.findOneByUserCode(userCode));

    }

    // private UserDTO convertToUserDTO(User user) {
    // return UserDTO.builder()
    // .userCode(user.getUserCode())
    // .fullName(user.getFullName())
    // .email(user.getEmail())
    // .dateOfBirth(user.getDateOfBirth())
    // .gender(user.getGender())
    // .address(user.getAddress())
    // .roleName(user.getRole().getNameRole())
    // .phone(user.getPhone())
    // .build();
    // }

    // public List<UserDTO> getAllUsers() {
    // List<User> users = this.userRepository.findAll();
    // return users.stream()
    // .map(this::convertToUserDTO)
    // .toList();
    // }

    // public User saveUser(User user) {
    // return this.userRepository.save(user);
    // }

    // public void deleteUser(String userCode) {
    // this.userRepository.deleteUserByUserCode(userCode);
    // }

    // public boolean isExistUserCode(String userCode) {
    // return this.userRepository.existsByUserCode(userCode);
    // }

}
