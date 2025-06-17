package vn.doan.lms.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import vn.doan.lms.service.implements_class.CloudinaryService;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    public FileUploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    // @PostMapping("/upload/file")
    // public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile
    // file) throws IOException {
    // return ResponseEntity.ok(cloudinaryService.uploadFile(file));
    // }

    // // Upload video (giữ nguyên)
    // @PostMapping("/upload/video")
    // public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile
    // file,
    // @RequestParam("folder") String folderName) throws IOException {
    // return ResponseEntity.ok(cloudinaryService.uploadVideo(file, folderName));
    // }

    // // ✨ THÊM MỚI: Upload document
    // @PostMapping("/upload/document")
    // public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile
    // file,
    // @RequestParam("folder") String folderName) {
    // try {
    // // Validate file type
    // String fileName = file.getOriginalFilename();
    // if (fileName == null || !isValidDocumentType(fileName)) {
    // return ResponseEntity.badRequest()
    // .body("Invalid file type. Only PDF, DOCX, XLSX files are allowed.");
    // }

    // // Validate file size (50MB limit for documents)
    // if (file.getSize() > 50 * 1024 * 1024) {
    // return ResponseEntity.badRequest()
    // .body("File size too large. Maximum 50MB allowed for documents.");
    // }

    // Map result = cloudinaryService.uploadDocument(file, folderName);
    // return ResponseEntity.ok(result);

    // } catch (IOException e) {
    // return ResponseEntity.status(500)
    // .body("Upload failed: " + e.getMessage());
    // }
    // }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", defaultValue = "uploads") String folderName) {
        try {
            // Validation
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body("No files provided");
            }

            // Validate file count (max 20 files)
            if (files.length > 20) {
                return ResponseEntity.badRequest().body("Too many files. Maximum 20 files allowed.");
            }

            // Validate each file
            List<String> errors = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    errors.add("File '" + file.getOriginalFilename() + "' is empty");
                }
                if (file.getSize() > 100 * 1024 * 1024) { // 100MB limit
                    errors.add("File '" + file.getOriginalFilename() + "' is too large (max 100MB)");
                }
            }

            if (!errors.isEmpty()) {

                return ResponseEntity.badRequest().body("Validation errors" + errors);
            }

            // Convert array to list
            List<MultipartFile> fileList = Arrays.asList(files);

            // Upload files
            List<Map> uploadResults;
            if (fileList.size() == 1) {
                // Single file
                Map result = cloudinaryService.uploadFileAuto(fileList.get(0), folderName);
                uploadResults = List.of(result);
            } else {
                // Multiple files
                uploadResults = cloudinaryService.uploadMultipleFiles(fileList, folderName);
            }

            // Count results
            long successCount = uploadResults.stream().filter(r -> !r.containsKey("error")).count();
            long failureCount = uploadResults.size() - successCount;

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", failureCount == 0);
            response.put("totalFiles", files.length);
            response.put("successCount", successCount);
            response.put("failureCount", failureCount);
            response.put("uploadedAt", Instant.now().toString());
            response.put("folder", folderName);
            // response.put("uploadedBy", "dothithuanhtlu"); // Current user

            if (files.length == 1 && failureCount == 0) {
                // Single file success - return data directly
                response.put("data", uploadResults.get(0));
            } else {
                // Multiple files or has errors - return full results
                response.put("results", uploadResults);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}