package vn.doan.lms.domain.dto;

import lombok.Data;

@Data
public class UpdateStudentScoreRequest {
    private Float midtermScore;
    private Float finalScore;
}
