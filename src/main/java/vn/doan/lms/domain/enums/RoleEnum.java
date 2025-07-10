package vn.doan.lms.domain.enums;

public enum RoleEnum {
    ADMIN("Admin"),
    TEACHER("Teacher"),
    STUDENT("Student");

    private final String nameRole;

    RoleEnum(String nameRole) {
        this.nameRole = nameRole;
    }

    public String getNameRole() {
        return nameRole;
    }
}
