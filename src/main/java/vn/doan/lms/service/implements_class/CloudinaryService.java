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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

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
            log.info("Starting deletion of Cloudinary folder: {}", folderPath);
            boolean hasErrors = false;

            // Xóa từng loại tài nguyên
            if (!deleteResourcesByType(folderPath, "raw"))
                hasErrors = true;
            if (!deleteResourcesByType(folderPath, "image"))
                hasErrors = true;
            if (!deleteResourcesByType(folderPath, "video"))
                hasErrors = true;

            // Xóa thư mục rỗng
            try {
                cloudinary.api().deleteFolder(folderPath, new HashMap<>());
                log.info("Successfully deleted folder: {}", folderPath);
            } catch (Exception e) {
                log.warn("Could not delete empty folder: {} - {}", folderPath, e.getMessage());
            }

            if (hasErrors) {
                log.warn("Some resources in folder {} could not be deleted", folderPath);
                return false;
            }

            log.info("All resources in folder {} deleted successfully", folderPath);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete folder: {} - Error: {}", folderPath, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xóa tất cả tài nguyên của một loại cụ thể trong thư mục trên Cloudinary
     * 
     * @param folderPath   Đường dẫn thư mục cần xóa (VD: "products/123")
     * @param resourceType Loại tài nguyên ("image", "video" hoặc "raw")
     * @return true nếu xóa thành công toàn bộ, false nếu có lỗi xảy ra
     */
    @SuppressWarnings("rawtypes")
    private boolean deleteResourcesByType(String folderPath, String resourceType) {
        try {
            log.info("Bắt đầu xóa {} tài nguyên trong thư mục: {}", resourceType, folderPath);

            // 1. LẤY DANH SÁCH TÀI NGUYÊN TỪ CLOUDINARY
            // Thiết lập các tham số request
            Map result = cloudinary.api().resources(ObjectUtils.asMap(
                    "type", "upload", // Chỉ lấy các tài nguyên được upload
                    "prefix", folderPath + "/", // Lọc theo tiền tố đường dẫn
                    "resource_type", resourceType, // Loại tài nguyên cần lấy
                    "max_results", 500)); // Giới hạn tối đa 500 kết quả

            // 2. XỬ LÝ KẾT QUẢ TRẢ VỀ
            List<Map> resources = (List<Map>) result.get("resources");

            if (resources != null && !resources.isEmpty()) {
                log.info("Tìm thấy {} {} tài nguyên cần xóa", resources.size(), resourceType);

                // 3. TRÍCH XUẤT DANH SÁCH PUBLIC_ID
                // Mỗi tài nguyên trên Cloudinary có một public_id duy nhất
                List<String> publicIds = resources.stream()
                        .map(resource -> (String) resource.get("public_id"))
                        .collect(Collectors.toList());

                // 4. XÓA THEO TỪNG BATCH (NHÓM NHỎ)
                boolean allDeleted = true; // Cờ kiểm tra toàn bộ quá trình
                int batchSize = 100; // Cloudinary giới hạn số lượng xóa mỗi lần gọi API

                for (int i = 0; i < publicIds.size(); i += batchSize) {
                    // 4.1. Tạo batch (nhóm nhỏ) các public_id cần xóa
                    int endIndex = Math.min(i + batchSize, publicIds.size());
                    List<String> batch = publicIds.subList(i, endIndex);

                    try {
                        log.debug("Xử lý batch từ {} đến {}", i, endIndex - 1);

                        // 4.2. GỌI API XÓA BATCH
                        Map deleteResult = cloudinary.api().deleteResources(batch,
                                ObjectUtils.asMap(
                                        "type", "upload",
                                        "resource_type", resourceType));

                        // 4.3. KIỂM TRA KẾT QUẢ XÓA
                        Map deleted = (Map) deleteResult.get("deleted");
                        if (deleted != null) {
                            for (String publicId : batch) {
                                String status = (String) deleted.get(publicId);
                                if (!"deleted".equals(status)) {
                                    log.warn("Xóa không thành công {} - Trạng thái: {}", publicId, status);
                                    allDeleted = false;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Lỗi khi xóa batch {} tài nguyên: {}", resourceType, e.getMessage());
                        allDeleted = false;
                    }
                }

                // 5. TRẢ VỀ KẾT QUẢ CUỐI CÙNG
                if (allDeleted) {
                    log.info("Đã xóa thành công tất cả {} {} tài nguyên", publicIds.size(), resourceType);
                } else {
                    log.warn("Một số {} tài nguyên chưa được xóa hoàn toàn", resourceType);
                }
                return allDeleted;

            } else {
                // Trường hợp không có tài nguyên nào
                log.info("Không tìm thấy {} tài nguyên trong thư mục", resourceType);
                return true; // Coi như thành công nếu không có gì để xóa
            }

        } catch (Exception e) {
            // Xử lý các lỗi tổng thể
            log.error("LỖI HỆ THỐNG khi xóa {} tài nguyên: {}", resourceType, e.getMessage());
            return false;
        }
    }

    // ✨ Delete single file by public ID - IMPROVED
    @SuppressWarnings("rawtypes")
    public boolean deleteFile(String publicId, String resourceType) {
        try {
            log.info("Deleting file: {} (type: {})", publicId, resourceType);

            Map<String, Object> options = new HashMap<>();
            options.put("type", "upload");
            options.put("resource_type", resourceType != null ? resourceType : "raw");

            Map result = cloudinary.api().deleteResources(Arrays.asList(publicId),
                    options);

            // Check deletion result
            Map deleted = (Map) result.get("deleted");
            if (deleted != null && deleted.containsKey(publicId)) {
                String status = (String) deleted.get(publicId);
                if ("deleted".equals(status)) {
                    log.info("File successfully deleted: {}", publicId);
                    return true;
                } else {
                    log.warn("File deletion status: {} for {}", status, publicId);
                    return false;
                }
            } else {
                log.warn("No deletion status returned for: {}", publicId);
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to delete file: {} - Error: {}", publicId, e.getMessage());
            return false;
        }
    }

    // ✨ ALTERNATIVE: Delete all resources in folder by prefix (Simpler approach)
    @SuppressWarnings("rawtypes")
    public boolean deleteFolderByPrefix(String folderPath) {
        try {
            log.info("Deleting all resources with prefix: {}", folderPath);

            // Delete by prefix for each resource type
            String[] resourceTypes = { "raw", "image", "video" };

            for (String resourceType : resourceTypes) {
                try {
                    Map<String, Object> deleteOptions = new HashMap<>();
                    deleteOptions.put("type", "upload");
                    deleteOptions.put("resource_type", resourceType);

                    // Use prefix to delete all resources starting with folderPath
                    cloudinary.api().deleteResourcesByPrefix(folderPath + "/", deleteOptions);
                    log.info("Deleted {} resources with prefix: {}", resourceType, folderPath);

                } catch (Exception e) {
                    log.warn("No {} resources found with prefix: {} - {}", resourceType,
                            folderPath, e.getMessage());
                }
            }

            return true;

        } catch (Exception e) {
            log.error("Failed to delete resources by prefix: {} - {}", folderPath,
                    e.getMessage());
            return false;
        }
    }
}