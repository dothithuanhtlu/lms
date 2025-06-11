package vn.doan.lms.domain.dto;

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
public class FileUploadRequestDTO {
    private String title;
    private String description;
    private Boolean isDownloadable;
    private String uploadedBy;
    private Integer displayOrder;
    private Boolean isPublic;
}
