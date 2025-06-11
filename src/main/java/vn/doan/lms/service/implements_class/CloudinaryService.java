package vn.doan.lms.service.implements_class;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.CloudinaryUploadResult;
import vn.doan.lms.util.error.BadRequestExceptionCustom;

@Service
@AllArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload file to Cloudinary
     * 
     * @param file   MultipartFile to upload
     * @param folder Folder to organize files in Cloudinary
     * @return CloudinaryUploadResult with upload information
     */
    public CloudinaryUploadResult uploadFile(MultipartFile file, String folder) {
        try {
            validateFile(file);

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto", // Auto-detect resource type (image, video, raw)
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false);

            // Special handling for video files
            if (isVideoFile(file)) {
                uploadParams.put("resource_type", "video");
                uploadParams.put("eager", "mp4"); // Convert to mp4 for better compatibility
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            log.info("File uploaded successfully to Cloudinary: {}", uploadResult.get("public_id"));
            return CloudinaryUploadResult.fromCloudinaryResponse(uploadResult);

        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage());
            throw new BadRequestExceptionCustom("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Delete file from Cloudinary
     * 
     * @param publicId     Public ID of the file to delete
     * @param resourceType Type of resource (image, video, raw)
     * @return true if deletion was successful
     */
    public boolean deleteFile(String publicId, String resourceType) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> deleteParams = ObjectUtils.asMap(
                    "resource_type", resourceType != null ? resourceType : "auto");

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, deleteParams);
            String deleteResult = (String) result.get("result");

            log.info("File deletion result from Cloudinary: {} for public_id: {}", deleteResult, publicId);
            return "ok".equals(deleteResult);

        } catch (Exception e) {
            log.error("Error deleting file from Cloudinary: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate optimized URL for file delivery
     * 
     * @param publicId Public ID of the file
     * @param width    Width for image optimization (optional)
     * @param height   Height for image optimization (optional)
     * @param quality  Quality setting (optional)
     * @return Optimized URL
     */
    public String generateOptimizedUrl(String publicId, Integer width, Integer height, String quality) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> options = ObjectUtils.asMap();

            if (width != null)
                options.put("width", width);
            if (height != null)
                options.put("height", height);
            if (quality != null)
                options.put("quality", quality);

            options.put("crop", "fill");
            options.put("f_auto", true); // Auto format selection

            return cloudinary.url().generate(publicId);
        } catch (Exception e) {
            log.error("Error generating optimized URL: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generate thumbnail URL for videos
     * 
     * @param publicId Public ID of the video
     * @param time     Time in seconds for thumbnail (optional)
     * @return Thumbnail URL
     */
    public String generateVideoThumbnail(String publicId, Integer time) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> options = ObjectUtils.asMap(
                    "resource_type", "video",
                    "width", 300,
                    "height", 200,
                    "crop", "fill",
                    "f_auto", true);

            if (time != null) {
                options.put("start_offset", time + "s");
            }

            return cloudinary.url().generate(publicId);
        } catch (Exception e) {
            log.error("Error generating video thumbnail: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate uploaded file
     * 
     * @param file MultipartFile to validate
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestExceptionCustom("File is empty or null");
        }

        // Check file size (100MB limit)
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new BadRequestExceptionCustom("File size exceeds 100MB limit");
        }

        // Check if filename exists
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new BadRequestExceptionCustom("Filename is required");
        }

        // Validate file extension
        String fileExtension = getFileExtension(filename);
        if (!isAllowedFileType(fileExtension)) {
            throw new BadRequestExceptionCustom("File type not allowed: " + fileExtension);
        }
    }

    /**
     * Check if file is a video
     * 
     * @param file MultipartFile to check
     * @return true if file is video
     */
    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("video/")) {
            return true;
        }

        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = getFileExtension(filename).toLowerCase();
            return extension.matches("mp4|avi|mov|wmv|flv|webm|mkv|m4v");
        }

        return false;
    }

    /**
     * Get file extension from filename
     * 
     * @param filename Filename to extract extension from
     * @return File extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Check if file type is allowed
     * 
     * @param extension File extension
     * @return true if allowed
     */
    private boolean isAllowedFileType(String extension) {
        String allowedTypes = "pdf|doc|docx|ppt|pptx|xls|xlsx|txt|" +
                "jpg|jpeg|png|gif|bmp|svg|webp|" +
                "mp4|avi|mov|wmv|flv|webm|mkv|m4v|" +
                "mp3|wav|flac|aac|ogg";
        return extension.matches(allowedTypes);
    }
}
