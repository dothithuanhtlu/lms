package vn.doan.lms.domain.dto.user_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailAccountDTO {
    private String fullName;
    private String email;
    private String userCode;
    private String password;
}
