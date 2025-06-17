package vn.doan.lms.service.implements_class;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.AllArgsConstructor;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // // Upload image (giữ nguyên)
    // public Map uploadFile(MultipartFile file) throws IOException {
    // return cloudinary.uploader().upload(file.getBytes(),
    // Map.of());
    // }

    // // Upload video (giữ nguyên)
    // public Map uploadVideo(MultipartFile file, String folderName) throws
    // IOException {
    // return cloudinary.uploader().upload(file.getBytes(),
    // ObjectUtils.asMap(
    // "resource_type", "video",
    // "folder", folderName));
    // }

    // // ✨ THÊM MỚI: Upload document (PDF, DOCX, XLSX, etc.)
    // public Map uploadDocument(MultipartFile file, String folderName) throws
    // IOException {
    // return cloudinary.uploader().upload(file.getBytes(),
    // ObjectUtils.asMap(
    // "resource_type", "raw", // "raw" cho documents
    // "folder", folderName,
    // "use_filename", true, // Giữ tên file gốc
    // "unique_filename", false // Không tạo tên file unique
    // ));
    // }

    // public Map uploadFileAuto(MultipartFile file, String folderName) throws
    // IOException {
    // String contentType = file.getContentType();

    // if (contentType != null) {
    // if (contentType.startsWith("image/")) {
    // // Image
    // return cloudinary.uploader().upload(file.getBytes(),
    // ObjectUtils.asMap(
    // "resource_type", "image",
    // "folder", folderName));
    // } else if (contentType.startsWith("video/")) {
    // // Video
    // return cloudinary.uploader().upload(file.getBytes(),
    // ObjectUtils.asMap(
    // "resource_type", "video",
    // "folder", folderName));
    // } else {
    // // Document/Raw file
    // return cloudinary.uploader().upload(file.getBytes(),
    // ObjectUtils.asMap(
    // "resource_type", "raw",
    // "folder", folderName,
    // "use_filename", true,
    // "unique_filename", false));
    // }
    // }

    // // Default fallback to raw
    // return uploadDocument(file, folderName);
    // }

    // ✨ MAIN METHOD: Upload single file với auto detection
    public Map uploadFileAuto(MultipartFile file, String folderName) throws IOException {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        Map<String, Object> options = new HashMap<>();
        options.put("folder", folderName);
        options.put("use_filename", true);
        options.put("unique_filename", true);

        // Auto detect resource type
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                options.put("resource_type", "image");
                options.put("quality", "auto");
                options.put("fetch_format", "auto");
            } else if (contentType.startsWith("video/")) {
                options.put("resource_type", "video");
                options.put("quality", "auto");
            } else {
                options.put("resource_type", "raw");
            }
        } else {
            // Fallback by file extension
            if (fileName != null) {
                String lowerFileName = fileName.toLowerCase();
                if (isImageFile(lowerFileName)) {
                    options.put("resource_type", "image");
                } else if (isVideoFile(lowerFileName)) {
                    options.put("resource_type", "video");
                } else if (lowerFileName.endsWith(".pdf")) {
                    options.put("resource_type", "raw");
                    options.put("type", "upload");
                    // KHÔNG set flags = attachment => cho phép xem trực tiếp
                } else {
                    options.put("resource_type", "raw");
                    options.put("type", "upload");
                }
            } else {
                options.put("resource_type", "raw");
            }
        }

        return cloudinary.uploader().upload(file.getBytes(), options);
    }

    // ✨ Upload multiple files concurrently
    public List<Map> uploadMultipleFiles(List<MultipartFile> files, String folderName) {
        List<CompletableFuture<Map>> futures = new ArrayList<>();

        for (MultipartFile file : files) {
            CompletableFuture<Map> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return uploadFileAuto(file, folderName);
                } catch (IOException e) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", true);
                    errorResult.put("message", e.getMessage());
                    errorResult.put("filename", file.getOriginalFilename());
                    return errorResult;
                }
            });
            futures.add(future);
        }

        return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("error", true);
                        errorResult.put("message", "Upload failed: " + e.getMessage());
                        return errorResult;
                    }
                })
                .collect(Collectors.toList());
    }

    private boolean isImageFile(String fileName) {
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                fileName.endsWith(".webp") || fileName.endsWith(".bmp");
    }

    private boolean isVideoFile(String fileName) {
        return fileName.endsWith(".mp4") || fileName.endsWith(".avi") ||
                fileName.endsWith(".mov") || fileName.endsWith(".wmv") ||
                fileName.endsWith(".flv") || fileName.endsWith(".webm");
    }
}
