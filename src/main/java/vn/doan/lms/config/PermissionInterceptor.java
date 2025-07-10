package vn.doan.lms.config;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import vn.doan.lms.domain.CustomResponse;
import vn.doan.lms.domain.User;
import vn.doan.lms.domain.enums.Permission;
import vn.doan.lms.domain.enums.PermissionRole;
import vn.doan.lms.service.implements_class.UserService;
import vn.doan.lms.util.SecurityUtil;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor kiểm tra quyền truy cập của người dùng dựa trên vai trò và các
 * quyền (permissions).
 * 
 * Mỗi request đến sẽ được kiểm tra xem người dùng có quyền thực hiện hành động
 * tương ứng không.
 * Nếu không có quyền, trả về HTTP status 403 Forbidden.
 * Nếu chưa đăng nhập, trả về HTTP status 401 Unauthorized.
 */
@Component
@AllArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    public static final String EMPTY_STRING = "";

    private final UserService userService;
    private final ObjectMapper objectMapper;

    // Dùng để so khớp các pattern đường dẫn có biến động (ví dụ
    // /students/{studentCode})
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final Logger logger = LoggerFactory.getLogger(PermissionInterceptor.class);

    /**
     * Xử lý trước khi controller được gọi, để kiểm tra quyền truy cập.
     * 
     * @param request  đối tượng HttpServletRequest của request hiện tại.
     * @param response đối tượng HttpServletResponse để trả về cho client.
     * @param handler  handler sẽ xử lý request, thường là controller.
     * @return true nếu được phép tiếp tục xử lý, false nếu bị chặn.
     * @throws Exception nếu xảy ra lỗi trong quá trình kiểm tra.
     */
    @Override
    @Transactional
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        // Lấy thông tin username (studentCode) của người dùng hiện tại từ
        // SecurityContext
        String userCode = SecurityUtil.getCurrentUserLogin().orElse(PermissionInterceptor.EMPTY_STRING);

        logger.debug("Processing request: URI={}, Method={}", requestURI, httpMethod);

        // Nếu chưa đăng nhập (không lấy được studentCode)
        if (userCode.isEmpty()) {
            logger.warn("No user authenticated");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized", "Bạn cần đăng nhập để truy cập tài nguyên này");
            return false; // chặn request tiếp tục
        }

        // Lấy thông tin Student từ database theo studentCode
        User user = this.userService.getUserByUserCode(userCode);
        if (user == null) {
            logger.warn("Student not found: {}", userCode);
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                    "Forbidden", "Không tìm thấy thông tin người dùng");
            return false; // chặn request tiếp tục
        }

        // Lấy role của user
        String role = user.getRole().getNameRole().toUpperCase();

        // Lấy danh sách quyền được phép theo role
        List<Permission> permissions = PermissionRole.getPermissionsForRole(role);

        // Chuyển method HTTP thành enum HttpMethod để so sánh
        HttpMethod requestHttpMethod = HttpMethod.valueOf(httpMethod);

        logger.debug("Checking permissions for role {}: {}", role, permissions);

        // Kiểm tra xem trong danh sách permission có cái nào phù hợp với request này
        // không
        boolean hasPermission = permissions.stream().anyMatch(permission -> {
            // Kiểm tra method HTTP có trùng không
            boolean methodMatches = permission.getHttpMethod() == requestHttpMethod;

            // Kiểm tra path có match với pattern trong permission không
            boolean pathMatches = pathMatcher.match(permission.getEndpointPattern(), requestURI);

            // Nếu không phải trường hợp đặc biệt trên thì chỉ cần method và path khớp là ok
            return methodMatches && pathMatches;
        });

        if (hasPermission) {
            logger.debug("Permission granted for URI: {}", requestURI);
            return true; // cho phép request tiếp tục
        }

        // Nếu không có quyền
        logger.warn("Permission denied for URI: {}", requestURI);
        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                "FORBIDDEN", "Access denied. You don't have permission to access this resource");
        return false; // chặn request tiếp tục
    }

    /**
     * Gửi error response theo format CustomResponse
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode,
            String error, String message) throws IOException {
        CustomResponse<Object> errorResponse = new CustomResponse<>();
        errorResponse.setStatusCode(statusCode);
        errorResponse.setError(error);
        errorResponse.setMessage(message);
        errorResponse.setData(null);

        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * Phương thức lấy biến động studentCode từ URI dựa trên pattern endpoint.
     * Ví dụ pattern: /students/{studentCode}
     * URI thực tế: /students/12345
     * => Trả về "12345"
     * 
     * @param requestURI URI thực tế của request.
     * @param pattern    pattern endpoint định nghĩa biến.
     * @return giá trị studentCode nếu tìm được, null nếu không tìm được.
     */
    // private String extractStudentCode(String requestURI, String pattern) {
    // Map<String, String> variables =
    // pathMatcher.extractUriTemplateVariables(pattern, requestURI);
    // return variables.get("studentCode");
    // }
}