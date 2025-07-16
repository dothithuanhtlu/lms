package vn.doan.lms.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.util.Base64;

import vn.doan.lms.domain.dto.ResLoginDTO;

@Service
public class SecurityUtil {
    private final JwtEncoder jwtEncoder;

    /** Thuật toán mã hóa JWT - sử dụng HMAC SHA-512 */
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    /** Khóa bí mật để ký JWT (dạng base64) */
    @Value("${lms.jwt.base64-secret}")
    private String jwtKey;

    /** Thời gian hết hạn của access token (tính bằng giây) */
    @Value("${lms.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    /** Thời gian hết hạn của refresh token (tính bằng giây) */
    @Value("${lms.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    /**
     * Constructor với dependency injection
     * 
     * @param jwtEncoder encoder để tạo JWT
     */
    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * Tạo access token JWT
     * 
     * @param userCode mã người dùng (subject)
     * @param dto      thông tin người dùng đăng nhập
     * @return chuỗi JWT
     */
    public String createAccessToken(String userCode, ResLoginDTO.UserLogin dto) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        // Lấy danh sách quyền dựa trên role name từ enum PermissionRole
        List<String> listAuthority = PermissionUtil.getPermissionsByRole(dto.getRoleName());

        // Xây dựng claims (nội dung) của token
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now) // Thời gian phát hành
                .expiresAt(validity) // Thời gian hết hạn
                .subject(userCode) // Mã người dùng
                .claim("user", dto) // Thông tin người dùng
                .claim("permission", listAuthority) // Danh sách quyền
                .build();

        // Thiết lập header với thuật toán mã hóa
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        // Mã hóa và trả về token
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    /**
     * Tạo refresh token JWT
     * 
     * @param userCode mã người dùng (subject)
     * @param dto      thông tin người dùng đăng nhập
     * @return chuỗi JWT
     */
    public String createRefreshToken(String userCode, ResLoginDTO dto) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        // Xây dựng claims (nội dung) của token
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now) // Thời gian phát hành
                .expiresAt(validity) // Thời gian hết hạn
                .subject(userCode) // Mã người dùng
                .claim("user", dto.getUser()) // Thông tin người dùng cơ bản
                .build();

        // Thiết lập header với thuật toán mã hóa
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        // Mã hóa và trả về token
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    /**
     * Tạo secret key từ chuỗi base64 trong cấu hình
     * 
     * @return đối tượng SecretKey
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode(); // Giải mã base64
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }

    /**
     * Kiểm tra tính hợp lệ của refresh token
     * 
     * @param token chuỗi JWT cần kiểm tra
     * @return đối tượng Jwt nếu hợp lệ
     * @throws RuntimeException nếu token không hợp lệ
     */
    public Jwt checkValidRefreshToken(String token) {
        // Tạo decoder với secret key và thuật toán
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JWT_ALGORITHM).build();
        try {
            return jwtDecoder.decode(token); // Giải mã token
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy thông tin người dùng hiện tại từ SecurityContext
     * 
     * @return Optional chứa username nếu có
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    /**
     * Trích xuất principal từ đối tượng Authentication
     * 
     * @param authentication đối tượng Authentication
     * @return username hoặc null nếu không có
     */
    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            // Trường hợp sử dụng UserDetails của Spring Security
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            // Trường hợp sử dụng JWT
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            // Trường hợp principal là String
            return s;
        }
        return null;
    }
}