package vn.doan.lms.service.implements_class;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.CloudinaryUploadResult;
import vn.doan.lms.util.error.FileStorageException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryFileService {

    private final Cloudinary cloudinary;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public Map<String, Object> uploadVideo(MultipartFile file, Long lessonId) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", "lms/lessons/" + lessonId + "/videos",
                    "resource_type", "video",
                    "public_id", generatePublicId(file.getOriginalFilename()),

                    // Video-specific optimizations
                    "quality", "auto:good",
                    "fetch_format", "auto",

                    // Generate thumbnails
                    "eager", Arrays.asList(
                            Map.of("width", 300, "height", 200, "crop", "fill", "format", "jpg"),
                            Map.of("width", 800, "height", 450, "crop", "fill", "format", "jpg")),

                    "eager_async", true,
                    "notification_url", getWebhookUrl() // Đã fix
            );

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);

            // Add custom fields
            result.put("thumbnailUrl", generateThumbnailUrl((String) result.get("public_id")));

            return result;

        } catch (Exception e) {
            log.error("Failed to upload video to Cloudinary", e);
            throw new FileStorageException("Video upload failed: " + e.getMessage());
        }
    }

    public Map<String, Object> uploadDocument(MultipartFile file, Long lessonId) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", "lms/lessons/" + lessonId + "/documents",
                    "resource_type", "raw",
                    "public_id", generatePublicId(file.getOriginalFilename()),
                    "use_filename", true,
                    "unique_filename", false);

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);

            // Generate PDF preview if it's a PDF
            if (file.getContentType() != null && file.getContentType().equals("application/pdf")) {
                result.put("previewUrl", generatePdfPreview((String) result.get("public_id")));
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to upload document to Cloudinary", e);
            throw new FileStorageException("Document upload failed: " + e.getMessage());
        }
    }

    public Map<String, Object> uploadImage(MultipartFile file, Long lessonId) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", "lms/lessons/" + lessonId + "/images",
                    "public_id", generatePublicId(file.getOriginalFilename()),

                    // Image optimizations
                    "quality", "auto:good",
                    "fetch_format", "auto",
                    "flags", "progressive",

                    // Generate multiple sizes
                    "eager", Arrays.asList(
                            Map.of("width", 150, "height", 150, "crop", "fill"),
                            Map.of("width", 400, "height", 300, "crop", "fill"),
                            Map.of("width", 800, "height", 600, "crop", "fill")));

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);

            // Add thumbnail URL
            result.put("thumbnailUrl", generateImageUrl((String) result.get("public_id"), 150, 150));

            return result;

        } catch (Exception e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new FileStorageException("Image upload failed: " + e.getMessage());
        }
    }

    // Utility methods - ĐÃ SỬA
    private String generatePublicId(String filename) {
        // Java thuần, không cần FilenameUtils
        String nameWithoutExt;
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            nameWithoutExt = filename.substring(0, lastDotIndex);
        } else {
            nameWithoutExt = filename;
        }
        return nameWithoutExt + "_" + System.currentTimeMillis();
    }

    private String generateThumbnailUrl(String publicId) {
        return cloudinary.url()
                .resourceType("video")
                .publicId(publicId)
                .format("jpg")
                .transformation(new Transformation()
                        .width(300)
                        .height(200)
                        .crop("fill")
                        .quality("auto:good"))
                .generate();
    }

    private String generatePdfPreview(String publicId) {
        return cloudinary.url()
                .resourceType("image")
                .publicId(publicId)
                .format("jpg")
                .transformation(new Transformation()
                        .width(400)
                        .height(600)
                        .crop("fill")
                        .page(1))
                .generate();
    }

    private String generateImageUrl(String publicId, int width, int height) {
        return cloudinary.url()
                .publicId(publicId)
                .transformation(new Transformation()
                        .width(width)
                        .height(height)
                        .crop("fill")
                        .quality("auto:good"))
                .generate();
    }

    // ĐÃ THÊM - Webhook URL
    private String getWebhookUrl() {
        return baseUrl + "/api/webhooks/cloudinary";
    }

    // Delete file
    public void deleteFile(String publicId, String resourceType) {
        try {
            Map<String, Object> params = ObjectUtils.asMap("resource_type", resourceType);
            cloudinary.uploader().destroy(publicId, params);
        } catch (Exception e) {
            log.error("Failed to delete file from Cloudinary: {}", publicId, e);
        }
    }
}