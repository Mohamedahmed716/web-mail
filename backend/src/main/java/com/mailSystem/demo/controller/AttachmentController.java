package com.mailSystem.demo.controller;

import com.mailSystem.demo.dal.FileAccessLayer;
import com.mailSystem.demo.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/attachments")
@CrossOrigin
public class AttachmentController {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email) {

        try {
            fileAccessLayer.saveAttachment(file, email);
            return ResponseEntity.ok(file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to upload file: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String file,
            @RequestParam String email,
            @RequestParam(defaultValue = "view") String mode) {

        try {
            Path filePath = Paths.get(Constants.DATA_DIR, email, "Attachments", file);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {

                String contentType;
                String lowerName = file.toLowerCase();

                if (lowerName.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (lowerName.endsWith(".png")) {
                    contentType = "image/png";
                } else {
                    contentType = Files.probeContentType(filePath);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                }

                String disposition = "attachment";

                if (!"download".equals(mode)) {
                    if (contentType.equals("application/pdf") || contentType.startsWith("image/")) {
                        disposition = "inline";
                    }
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);

            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}