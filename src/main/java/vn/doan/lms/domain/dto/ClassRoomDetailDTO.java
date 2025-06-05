package vn.doan.lms.domain.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassRoomDetailDTO {
    private long id;
    private String className;
    private String description;
    private Integer maxStudents;
    private Integer currentStudents;

    // Thông tin giáo viên chủ nhiệm
    private UserDTO advisor;

    // Thông tin chuyên ngành
    private MajorDTO major;

    // Danh sách sinh viên
    private List<UserDTO> students;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private long id;
        private String userCode;
        private String email;
        private String fullName;
        private String gender;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorDTO {
        private long id;
        private String majorName;
        private String description;
    }
}