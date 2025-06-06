package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassRoomListDTO {
    private long id;
    private String className;
    private String description;
    private Integer maxStudents;
    private Integer currentStudents;
    private String advisorName;
    private String advisorUserCode;
    private String majorCode;
    private String majorName;
}