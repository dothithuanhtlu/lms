package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.doan.lms.domain.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private long id;
    private String userCode;
    private String email;
    private String fullName;
    private RoleDTO role;

    public static UserDTO mapToDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .userCode(user.getUserCode())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}