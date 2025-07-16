package vn.doan.lms.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.doan.lms.domain.CustomResponse;

/**
 * CustomAuthenticationEntryPoint - Xử lý lỗi xác thực JWT
 * 
 * Class này implement AuthenticationEntryPoint để customize response khi:
 * - JWT token không hợp lệ (expired, malformed, missing)
 * - User chưa đăng nhập hoặc token bị từ chối
 * - Lỗi xác thực OAuth2 Resource Server
 * 
 * Thay vì trả về lỗi default của Spring Security, class này trả về
 * CustomResponse format thống nhất với toàn bộ hệ thống
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Delegate để xử lý OAuth2 Bearer Token authentication entry point
     * Sử dụng BearerTokenAuthenticationEntryPoint mặc định của Spring Security
     * để xử lý các lỗi liên quan đến Bearer token trước khi customize response
     */
    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    /**
     * ObjectMapper để chuyển đổi object thành JSON response
     * Dùng để serialize CustomResponse object thành JSON string
     */
    private final ObjectMapper mapper;

    /**
     * Constructor injection cho ObjectMapper
     * 
     * @param mapper ObjectMapper instance để serialize JSON response
     */
    public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Method chính xử lý authentication entry point
     * Được gọi khi có lỗi xác thực JWT token
     * 
     * @param request       HttpServletRequest - HTTP request hiện tại
     * @param response      HttpServletResponse - HTTP response để trả về client
     * @param authException AuthenticationException - Exception chứa thông tin lỗi
     *                      xác thực
     * @throws IOException      Nếu có lỗi I/O khi ghi response
     * @throws ServletException Nếu có lỗi servlet
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        // Gọi delegate để xử lý OAuth2 Bearer Token authentication trước
        // Điều này đảm bảo tuân thủ OAuth2 standard và set các header cần thiết
        this.delegate.commence(request, response, authException);

        // Set content type cho response là JSON với encoding UTF-8
        // Đảm bảo client nhận được response dạng JSON và hiển thị đúng tiếng Việt
        response.setContentType("application/json;charset=UTF-8");

        // Tạo CustomResponse object theo format thống nhất của hệ thống
        CustomResponse<Object> res = new CustomResponse<>();

        // Set status code là 401 UNAUTHORIZED
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        // Lấy error message chi tiết từ exception
        // Ưu tiên lấy message từ cause (nguyên nhân gốc) nếu có
        // Nếu không có cause thì lấy message từ chính exception
        String errorMessage = Optional.ofNullable(authException.getCause())
                .map(Throwable::getMessage) // Lấy message từ cause
                .orElse(authException.getMessage()); // Fallback to main exception message

        // Set error message chi tiết (technical error)
        res.setError(errorMessage);

        // Set message thân thiện cho user (user-friendly message)
        res.setMessage("Token không hợp lệ (hết hạn, không đúng định dạng, hoặc không truyền JWT ở header)");

        // Serialize CustomResponse object thành JSON và ghi vào response
        // Client sẽ nhận được JSON response theo format chuẩn của hệ thống
        mapper.writeValue(response.getWriter(), res);
    }

}
