package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDocumentResponse {

    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Boolean isDownloadable;
}
