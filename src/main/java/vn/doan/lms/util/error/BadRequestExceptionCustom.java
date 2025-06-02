package vn.doan.lms.util.error;

public class BadRequestExceptionCustom extends RuntimeException {

    /**
     * Khởi tạo ngoại lệ với thông điệp lỗi cụ thể.
     *
     * @param message thông điệp mô tả lỗi khi thực thi stored procedure
     */
    public BadRequestExceptionCustom(String message) {
        super(message);
    }
}