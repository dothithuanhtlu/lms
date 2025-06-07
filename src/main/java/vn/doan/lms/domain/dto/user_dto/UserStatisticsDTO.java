package vn.doan.lms.domain.dto.user_dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsDTO {
    private long totalUsers;
    private long totalAdmins;
    private long totalTeachers;
    private long totalStudents;
}
