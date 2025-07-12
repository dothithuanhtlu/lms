package vn.doan.lms.domain.dto.user_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailAccounttUpdateDTO {
    private String email;
    private String fullName;
    private String role;
}
