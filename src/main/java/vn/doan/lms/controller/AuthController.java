package vn.doan.lms.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.dto.LoginDTO;
import vn.doan.lms.domain.dto.ResLoginDTO;
import vn.doan.lms.service.implements_class.UserService;
import vn.doan.lms.util.SecurityUtil;
import vn.doan.lms.util.error.BadRequestExceptionCustom;

import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    @Value("${lms.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        // Tạo một token chứa username và password của người dùng để xác thực.
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        // xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // create a token
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO();
        User currentUser = this.userService.getUserByUserCode(loginDTO.getUsername());
        if (currentUser != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
            userLogin.setId(currentUser.getId());
            userLogin.setUserCode(currentUser.getUserCode());
            userLogin.setFullName(currentUser.getFullName());
            userLogin.setEmail(currentUser.getEmail());
            res.setUser(userLogin);
        }
        String access_token = this.securityUtil.createAccessToken(authentication.getName(), res.getUser());
        res.setAccessToken(access_token);
        String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getUsername(), res);
        // update refresh token in database
        this.userService.updateUserRefreshToken(refreshToken, loginDTO.getUsername());
        // set cookie
        ResponseCookie resCookies = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    @GetMapping("/auth/account")
    public ResponseEntity<ResLoginDTO.UserLogin> getAccount() {
        String userCode = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        User currentUser = this.userService.getUserByUserCode(userCode);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        if (currentUser != null) {
            userLogin.setId(currentUser.getId());
            userLogin.setUserCode(currentUser.getUserCode());
            userLogin.setFullName(currentUser.getFullName());
            userLogin.setEmail(currentUser.getEmail());
        }
        return ResponseEntity.ok(userLogin);

    }

    @GetMapping("/auth/refresh")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refreshToken", defaultValue = "abc") String refreshToken) {
        if (refreshToken.equals("abc") || refreshToken.isEmpty()) {
            throw new BadRequestExceptionCustom("refreshToken is missing");
        }
        // check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String userCode = decodedToken.getSubject();
        // get user from database (by referesh token and email)
        User currentUser = this.userService.getUserByRefreshTokenAndUserCode(userCode, refreshToken);
        if (currentUser == null) {
            throw new BadRequestExceptionCustom("Refresh token is invalid or user not found");
        }
        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.getUserByUserCode(userCode);
        if (currentUser != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
            userLogin.setId(currentUserDB.getId());
            userLogin.setUserCode(currentUserDB.getUserCode());
            userLogin.setFullName(currentUserDB.getFullName());
            userLogin.setEmail(currentUserDB.getEmail());
            res.setUser(userLogin);
        }
        String access_token = this.securityUtil.createAccessToken(userCode, res.getUser());
        res.setAccessToken(access_token);
        String newRefreshToken = this.securityUtil.createRefreshToken(userCode, res);
        // update refresh token in database
        this.userService.updateUserRefreshToken(newRefreshToken, userCode);
        // set cookie
        ResponseCookie resCookies = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        String userCode = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        if (userCode.equals("")) {
            throw new BadRequestExceptionCustom("Access token is invalid or expired");
        }
    }

}
