package com.cm.sanchalak.service.storage.impl;

import com.cm.sanchalak.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Local file storage provider for development environments.
 * Stores files in a local directory.
 */
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalStorageProvider implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageProvider.class);
    private final String uploadDir = "uploads";

    public LocalStorageProvider() {
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            logger.info("LocalStorageProvider initialized with directory: {}", path.toAbsolutePath());
        } catch (Exception e) {
            logger.error("Failed to initialize LocalStorageProvider", e);
        }
    }

    @Override
    public String generateUploadUrl(String objectKey, String contentType, int expiryMinutes) {
        // For local storage, we just return the object key or a local path
        // representation
        // In a real local provider, this would point to a local server endpoint
        return "/api/storage/upload/" + objectKey;
    }

    @Override
    public String generateDownloadUrl(String objectKey, int expiryMinutes) {
        return "/api/storage/download/" + objectKey;
    }

    @Override
    public void deleteFile(String objectKey) {
        try {
            Path path = Paths.get(uploadDir, objectKey);
            Files.deleteIfExists(path);
            logger.info("Deleted local file: {}", objectKey);
        } catch (Exception e) {
            logger.error("Failed to delete local file: {}", objectKey, e);
        }
    }

    @Override
    public String getPublicUrl(String objectKey) {
        return "/uploads/" + objectKey;
    }
}
