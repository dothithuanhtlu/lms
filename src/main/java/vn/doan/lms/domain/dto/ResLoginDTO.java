package vn.doan.lms.domain.dto;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class ResLoginDTO {
    private String accessToken;
    private UserLogin user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLogin {
        private long id;
        private String userCode;
        private String fullName;
        private String email;
    }
}
