package vn.doan.lms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDocumentDTO {
    private Long id;
    private String filePath;
    private String originalFileName;

    // File format information
    private String fileExtension;
    private String mimeType;
    private Long fileSize;
    private String documentType; // PDF, IMAGE, VIDEO, DOCUMENT, etc.

    // Display information
    private String title;
    private String description;
    private Boolean isDownloadable;

    // Convenience constructor for basic info
    public AssignmentDocumentDTO(Long id, String filePath, String originalFileName) {
        this.id = id;
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.fileExtension = extractFileExtension(originalFileName);
        this.mimeType = getMimeTypeFromExtension(this.fileExtension);
        this.documentType = getDocumentTypeFromExtension(this.fileExtension);
        this.isDownloadable = true;
        this.title = originalFileName;
    }

    // Helper methods
    private String extractFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "unknown";
    }

    private String getMimeTypeFromExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt" -> "text/plain";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/avi";
            case "mov" -> "video/quicktime";
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            case "7z" -> "application/x-7z-compressed";
            default -> "application/octet-stream";
        };
    }

    private String getDocumentTypeFromExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case "pdf" -> "PDF";
            case "doc", "docx" -> "DOCUMENT";
            case "xls", "xlsx" -> "SPREADSHEET";
            case "ppt", "pptx" -> "PRESENTATION";
            case "txt" -> "TEXT";
            case "jpg", "jpeg", "png", "gif", "webp" -> "IMAGE";
            case "mp4", "avi", "mov" -> "VIDEO";
            case "mp3", "wav" -> "AUDIO";
            case "zip", "rar", "7z" -> "ARCHIVE";
            default -> "OTHER";
        };
    }
}
