package vn.doan.lms.service.implements_class;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import vn.doan.lms.domain.dto.Meta;
import vn.doan.lms.domain.dto.ResultPaginationDTO;
import vn.doan.lms.domain.dto.user_dto.AdminDTO;
import vn.doan.lms.domain.dto.user_dto.StudentDTO;
import vn.doan.lms.domain.dto.user_dto.StudentDTOUpdate;
import vn.doan.lms.domain.dto.user_dto.TeacherDTO;
import vn.doan.lms.domain.dto.user_dto.TeacherSelectDTO;
import vn.doan.lms.domain.dto.user_dto.UserDTO;
import vn.doan.lms.domain.dto.user_dto.UserDTOCreate;
import vn.doan.lms.domain.dto.user_dto.UserStatisticsDTO;
import vn.doan.lms.repository.ClassRoomRepository;
import vn.doan.lms.repository.DepartmentRepository;
import vn.doan.lms.repository.RoleRepository;
import vn.doan.lms.repository.UserRepository;
import vn.doan.lms.util.SecurityUtil;
import vn.doan.lms.util.error.BadRequestExceptionCustom;
import vn.doan.lms.util.error.ConflictExceptionCustom;
import vn.doan.lms.util.error.EmailValidationException;
import vn.doan.lms.util.error.ResourceNotFoundException;
import vn.doan.lms.util.error.StoredProcedureFailedException;
import vn.doan.lms.util.error.UserCodeValidationException;

@Getter
@Setter
@AllArgsConstructor
@Service
public class UserService {
    private static final String ROLE_TEACHER = "Teacher";
    private static final String ROLE_STUDENT = "student";
    private static final String ROLE_ADMIN = "Admin";
    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private EntityManager entityManager;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;

    public List<TeacherSelectDTO> getTeachersForSelect() {
        List<User> users = this.userRepository.findAll();
        if (users == null || users.isEmpty()) {
            throw new ResourceNotFoundException("No teachers found for the given department ID");
        }
        return users.stream()
                .filter(user -> user.getRole() != null && ROLE_TEACHER.equals(user.getRole().getNameRole()))
                .map(TeacherSelectDTO::new)
                .toList();
    }

    public UserStatisticsDTO getUserStatistics() {
        long totalUsers = userRepository.count();
        long totalTeachers = userRepository.countByRole_NameRole(ROLE_TEACHER);
        long totalStudents = userRepository.countByRole_NameRole(ROLE_STUDENT);
        long totalAdmins = userRepository.countByRole_NameRole(ROLE_ADMIN); // Adjust if you have an admin role

        return UserStatisticsDTO.builder()
                .totalUsers(totalUsers)
                .totalTeachers(totalTeachers)
                .totalStudents(totalStudents)
                .totalAdmins(totalAdmins) // Assuming no admin role is counted here, adjust if needed
                .build();
    }

    public ResultPaginationDTO getAllUsers(Optional<String> currentOptional, Optional<String> pageSizeOptional) {

        // Kiểm tra xem cả 2 parameter có tồn tại không
        if (!currentOptional.isPresent() || !pageSizeOptional.isPresent()) {
            throw new BadRequestExceptionCustom("Both current and pageSize must be provided");
        }

        try {
            // Ép kiểu từ String sang int
            int current = Integer.parseInt(currentOptional.get());
            int pageSize = Integer.parseInt(pageSizeOptional.get());

            // Kiểm tra current và pageSize phải hợp lệs
            if (current <= 0 || pageSize <= 0) {
                throw new BadRequestExceptionCustom("Page and pageSize must be positive integers");
            }

            // Tạo đối tượng phân trang (Pageable)
            Pageable pageable = PageRequest.of(current - 1, pageSize);

            // Gọi repository để lấy dữ liệu
            Page<User> pageUsers = userRepository.findAll(pageable);

            // Map dữ liệu sang DTO
            List<UserDTO> userDTOList = pageUsers.getContent().stream()
                    .map(user -> UserDTO.builder()
                            .userCode(user.getUserCode())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .gender(user.getGender())
                            .roleName(user.getRole() != null ? user.getRole().getNameRole() : null)
                            .build())
                    .toList();

            // Tạo metadata phân trang
            Meta meta = new Meta();
            meta.setPage(pageUsers.getNumber());
            meta.setPageSize(pageUsers.getSize());
            meta.setPages(pageUsers.getTotalPages());
            meta.setTotal(pageUsers.getTotalElements());

            // Trả về ResultPaginationDTO
            ResultPaginationDTO result = new ResultPaginationDTO();
            result.setMeta(meta);
            result.setResult(userDTOList);
            return result;

        } catch (NumberFormatException e) {
            throw new BadRequestExceptionCustom("Invalid format for current or pageSize");
        }
    }

    public AdminDTO getAdminByUserCode(String userCode) {
        if (!isExistUserCode(userCode)) {
            throw new ResourceNotFoundException("User code is not exists");
        }
        User user = this.userRepository.findOneByUserCode(userCode);
        return AdminDTO.builder()
                .userCode(user.getUserCode())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .address(user.getAddress())
                .phone(user.getPhone())
                .roleName(user.getRole() != null ? user.getRole().getNameRole() : null)
                .build();
    }

    public List<TeacherDTO> getAllTeachers() {
        Role role = this.roleRepository.findOneByNameRole(ROLE_TEACHER);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found: " + ROLE_TEACHER);
        }
        List<User> list = this.userRepository.findAllByRoleId(role.getId());
        return list.stream()
                .map(user -> TeacherDTO.builder()
                        .userCode(user.getUserCode())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .dateOfBirth(user.getDateOfBirth())
                        .gender(user.getGender())
                        .address(user.getAddress())
                        .phone(user.getPhone())
                        .departmentCode(user.getDepartment() != null ? user.getDepartment().getDepartmentCode() : null)
                        .departmentName(user.getDepartment() != null ? user.getDepartment().getNameDepartment() : null)
                        .build())
                .toList();
    }

    public TeacherDTO getTeacherByUserCode(String userCode) {
        if (!isExistUserCode(userCode)) {
            throw new ResourceNotFoundException("User code is not exists");
        }
        User user = this.userRepository.findOneByUserCode(userCode);
        return TeacherDTO.builder()
                .userCode(user.getUserCode())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .address(user.getAddress())
                .phone(user.getPhone())
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getDepartmentCode() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getNameDepartment() : null)
                .build();
    }

    public User getUserByUserCode(String userCode) {
        if (!isExistUserCode(userCode)) {
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
                .roleName(user.getRole() != null ? user.getRole().getNameRole() : null)
                .className(user.getClassRoom().getClassName())
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

    private boolean isExistPhone(String phone) {
        return this.userRepository.existsByPhone(phone);
    }

    public UserDTOCreate saveUser(UserDTOCreate userDTOCreate) {
        if (isExistUserCode(userDTOCreate.getUserCode()) || isExistEmail(userDTOCreate.getEmail())
                || isExistPhone(userDTOCreate.getPhone())) {
            throw new ConflictExceptionCustom("User code or email or phone already exists");
        }
        String hashPass = passwordEncoder.encode(userDTOCreate.getPassword());
        userDTOCreate.setPassword(hashPass);
        User user = User.builder()
                .userCode(userDTOCreate.getUserCode())
                .email(userDTOCreate.getEmail())
                .fullName(userDTOCreate.getFullName())
                .dateOfBirth(userDTOCreate.getDateOfBirth())
                .gender(userDTOCreate.getGender())
                .address(userDTOCreate.getAddress())
                .phone(userDTOCreate.getPhone())
                .password(userDTOCreate.getPassword())
                .role(this.roleRepository.findOneByNameRole(userDTOCreate.getRoleName()))
                .classRoom(this.classRoomRepository.findByClassName(userDTOCreate.getClassName()))
                .department(this.departmentRepository.findOneByNameDepartment(userDTOCreate.getDepartmentName()))
                .build();
        this.userRepository.save(user);
        return userDTOCreate;
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

    // private StudentDTO convertToStudentDTO(User user) {
    // return StudentDTO.builder()
    // .userCode(user.getUserCode())
    // .fullName(user.getFullName())
    // .email(user.getEmail())
    // .dateOfBirth(user.getDateOfBirth())
    // .gender(user.getGender())
    // .address(user.getAddress())
    // .phone(user.getPhone())
    // .className(user.getClassRoom().getClassName())
    // .build();
    // }

    public StudentDTO getStudentByUserCode(String userCode) {
        if (!isExistUserCode(userCode)) {
            throw new ResourceNotFoundException("User code is not exists");
        }
        return convertToStudentDTO(this.userRepository.findOneByUserCode(userCode));

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
