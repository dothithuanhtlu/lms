package vn.doan.lms.domain;

import java.util.Map;

// Tạo file CloudinaryUploadResult.java
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloudinaryUploadResult {
    private String publicId;
    private String url;
    private String secureUrl;
    private String thumbnailUrl;
    private String previewUrl;
    private Integer duration;
    private String format;
    private Long bytes;
    private Integer width;
    private Integer height;
    private String resourceType;

    // Static method để convert từ Cloudinary response
    public static CloudinaryUploadResult fromCloudinaryResponse(Map<String, Object> response) {
        return CloudinaryUploadResult.builder()
                .publicId((String) response.get("public_id"))
                .url((String) response.get("url"))
                .secureUrl((String) response.get("secure_url"))
                .format((String) response.get("format"))
                .bytes(((Number) response.getOrDefault("bytes", 0)).longValue())
                .width(((Number) response.getOrDefault("width", 0)).intValue())
                .height(((Number) response.getOrDefault("height", 0)).intValue())
                .duration(((Number) response.getOrDefault("duration", 0)).intValue())
                .resourceType((String) response.get("resource_type"))
                .build();
    }
}