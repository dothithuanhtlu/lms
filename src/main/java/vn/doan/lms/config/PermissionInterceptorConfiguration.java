package vn.doan.lms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * Cấu hình đăng ký {@link PermissionInterceptor} cho ứng dụng Web.
 * 
 * Mục đích:
 * - Thêm interceptor kiểm tra quyền truy cập vào các request HTTP.
 * - Loại trừ các đường dẫn không cần kiểm tra quyền như /login, Swagger UI và
 * API docs.
 */
@Configuration
@RequiredArgsConstructor
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {

    /**
     * Interceptor xử lý kiểm tra quyền truy cập người dùng.
     */
    private final PermissionInterceptor permissionInterceptor;

    /**
     * Đăng ký các interceptor cho ứng dụng.
     * 
     * @param registry Registry dùng để thêm các interceptor.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                // Loại trừ các đường dẫn không cần kiểm tra quyền
                .excludePathPatterns(
                        "/login",
                        "/swagger-ui/**",
                        "/v3/api-docs/**", "/send-email", "/send-email-update");
    }
}