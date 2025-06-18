package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDocumentDTO {
    private Long id;
    private String filePath;
    private String originalFileName;
}
