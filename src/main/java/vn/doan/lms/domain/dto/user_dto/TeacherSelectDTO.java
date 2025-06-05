package vn.doan.lms.domain.dto.user_dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doan.lms.domain.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherSelectDTO {
    private long id;
    private String fullName;
    private String userCode;

    public TeacherSelectDTO(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.userCode = user.getUserCode();
    }
}
