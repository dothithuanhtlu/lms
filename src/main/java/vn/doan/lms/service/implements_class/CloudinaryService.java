package vn.doan.lms.service.implements_class;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public boolean deleteFolder(String folderPath) {
        try {
            log.info("🗑️ Deleting Cloudinary folder: {}", folderPath);

            // Delete all RAW resources in the folder
            deleteResourcesByType(folderPath, "raw");

            // Delete all IMAGE resources in the folder
            deleteResourcesByType(folderPath, "image");

            // Delete all VIDEO resources in the folder
            deleteResourcesByType(folderPath, "video");

            // Finally delete the empty folder with proper parameters
            try {
                Map<String, Object> folderOptions = new HashMap<>();
                cloudinary.api().deleteFolder(folderPath, folderOptions);
                log.info("✅ Successfully deleted folder: {}", folderPath);
            } catch (Exception e) {
                log.warn("⚠️ Could not delete empty folder: {} - {}", folderPath, e.getMessage());
                // This is often expected as folder might not exist or already be deleted
            }

            return true;

        } catch (Exception e) {
            log.error("❌ Failed to delete folder: {} - Error: {}", folderPath, e.getMessage(), e);
            return false;
        }
    }

    // ✨ Helper method to delete resources by type
    private void deleteResourcesByType(String folderPath, String resourceType) {
        try {
            log.info("🔄 Deleting {} resources in folder: {}", resourceType, folderPath);

            Map<String, Object> options = new HashMap<>();
            options.put("type", "upload");
            options.put("prefix", folderPath + "/");
            options.put("max_results", 500); // Maximum results per request

            Map result = cloudinary.api().resources(ObjectUtils.asMap(
                    "type", "upload",
                    "prefix", folderPath + "/",
                    "resource_type", resourceType,
                    "max_results", 500));

            List<Map> resources = (List<Map>) result.get("resources");

            if (resources != null && !resources.isEmpty()) {
                log.info("📂 Found {} {} files in folder to delete", resources.size(), resourceType);

                // Collect public IDs to delete
                List<String> publicIds = resources.stream()
                        .map(resource -> (String) resource.get("public_id"))
                        .collect(Collectors.toList());

                // Delete resources in batches (Cloudinary has limits)
                int batchSize = 100;
                for (int i = 0; i < publicIds.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, publicIds.size());
                    List<String> batch = publicIds.subList(i, endIndex);

                    Map<String, Object> deleteOptions = new HashMap<>();
                    deleteOptions.put("type", "upload");
                    deleteOptions.put("resource_type", resourceType);

                    Map deleteResult = cloudinary.api().deleteResources(batch, deleteOptions);
                    log.info("🗑️ Deleted batch of {} {} files", batch.size(), resourceType);
                }

                log.info("✅ Deleted total {} {} files from folder", publicIds.size(), resourceType);
            } else {
                log.info("📂 No {} files found in folder: {}", resourceType, folderPath);
            }

        } catch (Exception e) {
            log.warn("⚠️ Failed to delete {} resources in folder {}: {}", resourceType, folderPath, e.getMessage());
        }
    }

    // ✨ Delete single file by public ID - IMPROVED
    public boolean deleteFile(String publicId, String resourceType) {
        try {
            log.info("🗑️ Deleting file: {} (type: {})", publicId, resourceType);

            Map<String, Object> options = new HashMap<>();
            options.put("type", "upload");
            options.put("resource_type", resourceType != null ? resourceType : "raw");

            Map result = cloudinary.api().deleteResources(Arrays.asList(publicId), options);

            // Check deletion result
            Map deleted = (Map) result.get("deleted");
            if (deleted != null && deleted.containsKey(publicId)) {
                String status = (String) deleted.get(publicId);
                if ("deleted".equals(status)) {
                    log.info("✅ File successfully deleted: {}", publicId);
                    return true;
                } else {
                    log.warn("⚠️ File deletion status: {} for {}", status, publicId);
                    return false;
                }
            } else {
                log.warn("⚠️ No deletion status returned for: {}", publicId);
                return false;
            }

        } catch (Exception e) {
            log.error("❌ Failed to delete file: {} - Error: {}", publicId, e.getMessage());
            return false;
        }
    }

    // ✨ ALTERNATIVE: Delete all resources in folder by prefix (Simpler approach)
    public boolean deleteFolderByPrefix(String folderPath) {
        try {
            log.info("🗑️ Deleting all resources with prefix: {}", folderPath);

            // Delete by prefix for each resource type
            String[] resourceTypes = { "raw", "image", "video" };

            for (String resourceType : resourceTypes) {
                try {
                    Map<String, Object> deleteOptions = new HashMap<>();
                    deleteOptions.put("type", "upload");
                    deleteOptions.put("resource_type", resourceType);

                    // Use prefix to delete all resources starting with folderPath
                    Map result = cloudinary.api().deleteResourcesByPrefix(folderPath + "/", deleteOptions);
                    log.info("✅ Deleted {} resources with prefix: {}", resourceType, folderPath);

                } catch (Exception e) {
                    log.warn("⚠️ No {} resources found with prefix: {} - {}", resourceType, folderPath, e.getMessage());
                }
            }

            return true;

        } catch (Exception e) {
            log.error("❌ Failed to delete resources by prefix: {} - {}", folderPath, e.getMessage());
            return false;
        }
    }
}
