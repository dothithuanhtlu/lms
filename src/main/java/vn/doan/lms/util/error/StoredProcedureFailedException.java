package vn.doan.lms.util.error;

/**
 * Ngoại lệ runtime được ném khi thực thi stored procedure thất bại.
 * Sử dụng để xử lý các lỗi liên quan đến stored procedure trong hệ thống.
 */
public class StoredProcedureFailedException extends RuntimeException {

    /**
     * Khởi tạo ngoại lệ với thông điệp lỗi cụ thể.
     *
     * @param message thông điệp mô tả lỗi khi thực thi stored procedure
     */
    public StoredProcedureFailedException(String message) {
        super(message);
    }
}