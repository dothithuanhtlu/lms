package vn.doan.lms.util.error;

public class ResourceNotFoundException extends RuntimeException {

    /**
     * Khởi tạo ngoại lệ với thông điệp lỗi cụ thể.
     *
     * @param message thông điệp mô tả lỗi khi thực thi stored procedure
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
