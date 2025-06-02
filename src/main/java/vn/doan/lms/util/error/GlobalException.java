package vn.doan.lms.util.error;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolationException;
import vn.doan.lms.domain.CustomResponse;

@RestControllerAdvice
public class GlobalException {
    private final Logger logger = LoggerFactory.getLogger(GlobalException.class);

    /**
     * Xử lý các ngoại lệ liên quan đến validation custom như:
     * - {@link UserCodeValidationException}
     * - {@link EmailValidationException}
     * - {@link ClassNameValidationException}
     *
     * Các exception này thường được ném ra khi dữ liệu đầu vào không thỏa mãn điều
     * kiện nghiệp vụ
     * cụ thể (ví dụ: mã người dùng sai định dạng, email không hợp lệ, tên lớp không
     * hợp lệ).
     *
     * Khi một trong các exception trên được ném ra, phương thức này sẽ:
     * - Ghi log lỗi để phục vụ kiểm tra sau này
     * - Trả về phản hồi định dạng chuẩn {@link CustomResponse} cho client
     * - Thiết lập mã lỗi HTTP 404 (Not Found), báo rằng dữ liệu yêu cầu không hợp
     * lệ hoặc không tồn tại
     *
     * @param e Exception được ném ra từ controller hoặc service
     * @return ResponseEntity chứa thông tin lỗi định dạng theo chuẩn CustomResponse
     *         và HTTP status 404
     */
    @ExceptionHandler(value = { UserCodeValidationException.class, EmailValidationException.class,
            ClassNameValidationException.class })
    public ResponseEntity<CustomResponse<Object>> handleIdException(Exception e) {
        logger.error("Validation error: {}", e.getMessage(), e); // Ghi log lỗi
        CustomResponse<Object> res = new CustomResponse<>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError(e.getMessage());
        res.setMessage("Data invalid!");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }
    @ExceptionHandler(BadRequestExceptionCustom.class)
    public ResponseEntity<CustomResponse<Object>> handleBadRequest(BadRequestExceptionCustom e) {
        logger.error("Data invalid(bad request): {}", e.getMessage(), e);
        CustomResponse<Object> res = new CustomResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Data invalid!");
        res.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomResponse<Object>> handleResourceNotFound(ResourceNotFoundException e) {
        logger.error("Data invalid(not found): {}", e.getMessage(), e);
        CustomResponse<Object> res = new CustomResponse<>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError("Resource Not Found");
        res.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    // method bắt mọi lỗi validation: với lỗi trả về MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponse<Object>> validateError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();

        CustomResponse<Object> res = new CustomResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getBody().getDetail());

        List<String> errors = fieldErrors.stream().map(f -> f.getDefaultMessage()).collect(Collectors.toList());
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // xu ly exception cua username login khong ton tai va nhap sai thong tin login
    @ExceptionHandler(value = { UsernameNotFoundException.class,
            BadCredentialsException.class })
    public ResponseEntity<CustomResponse<Object>> handleLoginException(
            Exception ex) {
        CustomResponse<Object> res = new CustomResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("Loi");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Xử lý ngoại lệ khi vi phạm ràng buộc xác thực (ConstraintViolationException).
     * trước khi lưu vào db
     * 
     * @param e ngoại lệ ConstraintViolationException chứa thông tin lỗi xác thực
     * @return {@link ResponseEntity} chứa {@link CustomResponse} với mã trạng thái
     *         400 và danh sách lỗi
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomResponse<Object>> handleConstraintViolation(ConstraintViolationException e) {
        logger.error("Validation error: {}", e.getMessage(), e);
        CustomResponse<Object> res = new CustomResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Validation Error");

        // Lấy toàn bộ lỗi và gộp lại
        List<String> messages = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        res.setMessage(messages.size() > 1 ? messages : messages.get(0));
        return ResponseEntity.badRequest().body(res);
    }

    /**
     * Xử lý ngoại lệ khi lớp học đầy hoặc không thể tạo/cập nhật sinh viên
     * 
     * @param ex ngoại lệ RuntimeException từ stored procedure
     * @return {@link ResponseEntity} chứa {@link CustomResponse} với mã trạng thái
     *         400
     */
    @ExceptionHandler(value = { StoredProcedureFailedException.class })
    public ResponseEntity<CustomResponse<Object>> handleStoredProcedureException(RuntimeException ex) {
        logger.error("Stored procedure operation failed: {}", ex.getMessage(), ex);
        CustomResponse<Object> res = new CustomResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Operation Failed");
        res.setMessage(ex.getMessage());
        return ResponseEntity.badRequest().body(res);
    }

    /**
     * Xử lý các lỗi liên quan đến giao dịch cơ sở dữ liệu
     * 
     * @param ex ngoại lệ liên quan đến giao dịch cơ sở dữ liệu
     * @return {@link ResponseEntity} chứa {@link CustomResponse} với mã trạng thái
     *         500
     */
    @ExceptionHandler(value = { DataAccessException.class, TransactionException.class })
    public ResponseEntity<CustomResponse<Object>> handleDatabaseException(Exception ex) {
        logger.error("Database operation failed: {}", ex.getMessage(), ex);
        CustomResponse<Object> res = new CustomResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setError("Database Operation Failed");
        res.setMessage("An error occurred while accessing the database");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    /**
     * Xử lý các ngoại lệ RuntimeException chung.
     * Bắt những ngoại lệ không được xử lý bởi các handler cụ thể khác
     *
     * @param ex      ngoại lệ RuntimeException được ném
     * @param request thông tin yêu cầu HTTP
     * @return {@link ResponseEntity} chứa thông tin lỗi định dạng theo
     *         CustomResponse
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CustomResponse<Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Unexpected runtime error: {}", ex.getMessage(), ex);
        CustomResponse<Object> res = new CustomResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setError("Unexpected Error");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}
