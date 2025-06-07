package vn.doan.lms.util.error;

public class ConflictExceptionCustom extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConflictExceptionCustom(String message) {
        super(message);
    }

    public ConflictExceptionCustom(String message, Throwable cause) {
        super(message, cause);
    }

}
