package vn.doan.lms.util.error;

/**
 * Ngoại lệ tùy chỉnh được ném khi email không hợp lệ hoặc đã tồn tại trong hệ
 * thống.
 * Sử dụng để xử lý các lỗi liên quan đến xác thực email trong các thao tác với
 * sinh viên.
 */
public class EmailValidationException extends Exception {

    /**
     * Khởi tạo ngoại lệ với thông điệp lỗi cụ thể.
     *
     * @param message thông điệp mô tả lỗi liên quan đến email
     */
    public EmailValidationException(String message) {
        super(message);
    }
}