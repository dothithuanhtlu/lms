package vn.doan.lms.domain.dto;

import vn.doan.lms.domain.ClassRoom;

public class ClassRoomDTO {
    private String className;
    private String advisorName; // Tên giáo viên chủ nhiệm

    public ClassRoomDTO(ClassRoom classRoom) {
        this.className = classRoom.getClassName();
        this.advisorName = classRoom.getAdvisor() != null
                ? classRoom.getAdvisor().getFullName()
                : "Chưa bổ nhiệm";
    }
}