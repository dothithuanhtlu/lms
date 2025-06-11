package vn.doan.lms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.domain.CloudinaryUploadResult;
import vn.doan.lms.service.implements_class.CloudinaryService;
import vn.doan.lms.util.error.BadRequestExceptionCustom;

@RestController
@RequestMapping("/api/upload")
@AllArgsConstructor
@Slf4j
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    /**
     * General file upload endpoint
     * Can be used for profile pictures, course thumbnails, etc.
     */
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "general") String folder) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("File is empty");
            }

            CloudinaryUploadResult result = cloudinaryService.uploadFile(file, folder);

            log.info("File uploaded successfully: {}", result.getPublicId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (BadRequestExceptionCustom e) {
            log.error("Bad request during file upload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error during file upload");
        }
    }

    /**
     * Upload image specifically (with optimization)
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "images") String folder,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height,
            @RequestParam(required = false) String quality) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Validate file is an image
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("File must be an image");
            }

            CloudinaryUploadResult result = cloudinaryService.uploadFile(file, folder);

            // Generate optimized URL if dimensions provided
            if (width != null || height != null || quality != null) {
                String optimizedUrl = cloudinaryService.generateOptimizedUrl(
                        result.getPublicId(), width, height, quality);
                result = CloudinaryUploadResult.builder()
                        .publicId(result.getPublicId())
                        .url(optimizedUrl)
                        .secureUrl(optimizedUrl)
                        .format(result.getFormat())
                        .bytes(result.getBytes())
                        .width(result.getWidth())
                        .height(result.getHeight())
                        .resourceType(result.getResourceType())
                        .build();
            }

            log.info("Image uploaded successfully: {}", result.getPublicId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (BadRequestExceptionCustom e) {
            log.error("Bad request during image upload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error during image upload");
        }
    }

    /**
     * Upload video specifically
     */
    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "videos") String folder) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Validate file is a video
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest().body("File must be a video");
            }

            CloudinaryUploadResult result = cloudinaryService.uploadFile(file, folder);

            // Generate thumbnail for video
            String thumbnailUrl = cloudinaryService.generateVideoThumbnail(result.getPublicId(), null);
            result = CloudinaryUploadResult.builder()
                    .publicId(result.getPublicId())
                    .url(result.getUrl())
                    .secureUrl(result.getSecureUrl())
                    .thumbnailUrl(thumbnailUrl)
                    .format(result.getFormat())
                    .bytes(result.getBytes())
                    .width(result.getWidth())
                    .height(result.getHeight())
                    .duration(result.getDuration())
                    .resourceType(result.getResourceType())
                    .build();

            log.info("Video uploaded successfully: {}", result.getPublicId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (BadRequestExceptionCustom e) {
            log.error("Bad request during video upload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading video: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error during video upload");
        }
    }
}
