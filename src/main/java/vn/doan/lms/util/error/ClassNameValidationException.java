package vn.doan.lms.util.error;

/**
 * Ngoại lệ được ném khi tên lớp học không hợp lệ hoặc không tồn tại.
 * Sử dụng để xử lý các lỗi liên quan đến xác thực tên lớp trong các thao tác.
 */
public class ClassNameValidationException extends Exception {

    /**
     * Khởi tạo ngoại lệ với thông điệp lỗi cụ thể.
     *
     * @param message thông điệp mô tả lỗi liên quan đến tên lớp học
     */
    public ClassNameValidationException(String message) {
        super(message);
    }
}
