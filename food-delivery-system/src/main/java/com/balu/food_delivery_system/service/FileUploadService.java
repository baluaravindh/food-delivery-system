package com.balu.food_delivery_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String uploadFile(MultipartFile file) throws IOException {

        // Validate file is not empty
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Validate file is image
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;

        // Create directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file to directory
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File uploaded successfully: {}", fileName);

        return fileName;
    }

    public void deleteFile(String fileName) {

        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", fileName);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileName);
        }
    }
}
