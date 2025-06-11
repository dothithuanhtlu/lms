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
public class FileUploadResponseDTO {
    private boolean success;
    private String message;
    private LessonDocumentDTO document;
    private String error;

    // Static factory methods for convenience
    public static FileUploadResponseDTO success(LessonDocumentDTO document) {
        return FileUploadResponseDTO.builder()
                .success(true)
                .message("File uploaded successfully")
                .document(document)
                .build();
    }

    public static FileUploadResponseDTO error(String error) {
        return FileUploadResponseDTO.builder()
                .success(false)
                .error(error)
                .build();
    }
}
