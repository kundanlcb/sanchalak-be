# File Storage Abstraction Pattern

**Feature**: Provider-agnostic file storage for homework submissions and attachments  
**Purpose**: Enable easy switching between AWS S3, Azure Blob Storage, Google Cloud Storage, or other providers without code changes

---

## Architecture Overview

```
┌─────────────────────────────────┐
│  HomeworkSubmissionService      │
│  (Business Logic)               │
└────────────┬────────────────────┘
             │
             │ depends on interface
             ▼
┌─────────────────────────────────┐
│  FileStorageService             │◄─── Interface
│  - generateUploadUrl()          │
│  - generateDownloadUrl()        │
│  - deleteFile()                 │
└────────────┬────────────────────┘
             │
             │ implemented by
     ┌───────┴────────┐
     │                │
     ▼                ▼
┌──────────────┐  ┌──────────────────┐
│S3StorageProvider│  │AzureBlobStorageProvider│
│(AWS)         │  │(Azure)           │
└──────────────┘  └──────────────────┘
```

---

## Interface Definition

```java
package com.cm.sanchalak.service.storage;

import java.time.Duration;

/**
 * Abstraction for file storage providers.
 * Implementations: AWS S3, Azure Blob Storage, Google Cloud Storage
 */
public interface FileStorageService {
    
    /**
     * Generate presigned/SAS URL for file upload
     * @param fileName Original file name with extension
     * @param contentType MIME type (e.g., "image/jpeg")
     * @param expiryDuration URL validity duration
     * @return PresignedUploadUrl containing upload URL and unique file key
     */
    PresignedUploadUrl generateUploadUrl(String fileName, String contentType, Duration expiryDuration);
    
    /**
     * Generate presigned/SAS URL for file download
     * @param fileKey Unique file identifier returned from generateUploadUrl
     * @param expiryDuration URL validity duration
     * @return Download URL valid for specified duration
     */
    String generateDownloadUrl(String fileKey, Duration expiryDuration);
    
    /**
     * Delete file from storage
     * @param fileKey Unique file identifier
     * @return true if deleted successfully, false if file doesn't exist
     */
    boolean deleteFile(String fileKey);
    
    /**
     * Check if file exists in storage
     * @param fileKey Unique file identifier
     * @return true if file exists
     */
    boolean fileExists(String fileKey);
}
```

---

## DTO for Upload Response

```java
package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadUrl {
    
    /**
     * Presigned URL for direct upload from mobile app
     * Mobile client uploads to this URL via HTTP PUT
     */
    private String uploadUrl;
    
    /**
     * Unique file key/identifier in storage system
     * Store this in database (homework_submissions.submission_file_urls)
     */
    private String fileKey;
    
    /**
     * Expiry timestamp (ISO 8601)
     */
    private String expiresAt;
    
    /**
     * File size limit in bytes
     */
    private Long maxSizeBytes;
}
```

---

## AWS S3 Implementation

```java
package com.cm.sanchalak.service.storage.impl;

import com.cm.sanchalak.dto.PresignedUploadUrl;
import com.cm.sanchalak.service.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "file-storage.provider", havingValue = "s3", matchIfMissing = true)
@Slf4j
public class S3StorageProvider implements FileStorageService {
    
    @Value("${file-storage.s3.bucket-name}")
    private String bucketName;
    
    @Value("${file-storage.s3.region}")
    private String region;
    
    private final S3Presigner s3Presigner;
    
    public S3StorageProvider(
        @Value("${file-storage.s3.access-key}") String accessKey,
        @Value("${file-storage.s3.secret-key}") String secretKey,
        @Value("${file-storage.s3.region}") String region
    ) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Presigner = S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }
    
    @Override
    public PresignedUploadUrl generateUploadUrl(String fileName, String contentType, Duration expiryDuration) {
        String fileKey = "homework-submissions/" + UUID.randomUUID() + "/" + fileName;
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileKey)
            .contentType(contentType)
            .build();
        
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder -> builder
            .putObjectRequest(putObjectRequest)
            .signatureDuration(expiryDuration)
        );
        
        log.info("Generated S3 presigned upload URL for file: {}, expires in: {}", fileKey, expiryDuration);
        
        return new PresignedUploadUrl(
            presignedRequest.url().toString(),
            fileKey,
            Instant.now().plus(expiryDuration).toString(),
            10L * 1024 * 1024 // 10MB
        );
    }
    
    @Override
    public String generateDownloadUrl(String fileKey, Duration expiryDuration) {
        // Similar presigned GET URL generation
        // Returns URL valid for expiryDuration
        return s3Presigner.presignGetObject(builder -> builder
            .getObjectRequest(req -> req.bucket(bucketName).key(fileKey))
            .signatureDuration(expiryDuration)
        ).url().toString();
    }
    
    @Override
    public boolean deleteFile(String fileKey) {
        // S3 delete object implementation
        return true;
    }
    
    @Override
    public boolean fileExists(String fileKey) {
        // S3 head object to check existence
        return true;
    }
}
```

---

## Azure Blob Storage Implementation

```java
package com.cm.sanchalak.service.storage.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.cm.sanchalak.dto.PresignedUploadUrl;
import com.cm.sanchalak.service.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "file-storage.provider", havingValue = "azure")
@Slf4j
public class AzureBlobStorageProvider implements FileStorageService {
    
    @Value("${file-storage.azure.container-name}")
    private String containerName;
    
    private final BlobContainerClient containerClient;
    
    public AzureBlobStorageProvider(
        @Value("${file-storage.azure.connection-string}") String connectionString,
        @Value("${file-storage.azure.container-name}") String containerName
    ) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
    }
    
    @Override
    public PresignedUploadUrl generateUploadUrl(String fileName, String contentType, Duration expiryDuration) {
        String fileKey = "homework-submissions/" + UUID.randomUUID() + "/" + fileName;
        
        BlobClient blobClient = containerClient.getBlobClient(fileKey);
        
        // Generate SAS token for upload (write permission)
        BlobSasPermission sasPermission = new BlobSasPermission()
            .setWritePermission(true)
            .setCreatePermission(true);
        
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
            OffsetDateTime.now().plus(expiryDuration),
            sasPermission
        ).setContentType(contentType);
        
        String sasToken = blobClient.generateSas(sasValues);
        String uploadUrl = blobClient.getBlobUrl() + "?" + sasToken;
        
        log.info("Generated Azure SAS URL for file: {}, expires in: {}", fileKey, expiryDuration);
        
        return new PresignedUploadUrl(
            uploadUrl,
            fileKey,
            OffsetDateTime.now().plus(expiryDuration).toString(),
            10L * 1024 * 1024 // 10MB
        );
    }
    
    @Override
    public String generateDownloadUrl(String fileKey, Duration expiryDuration) {
        BlobClient blobClient = containerClient.getBlobClient(fileKey);
        
        BlobSasPermission sasPermission = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
            OffsetDateTime.now().plus(expiryDuration),
            sasPermission
        );
        
        String sasToken = blobClient.generateSas(sasValues);
        return blobClient.getBlobUrl() + "?" + sasToken;
    }
    
    @Override
    public boolean deleteFile(String fileKey) {
        BlobClient blobClient = containerClient.getBlobClient(fileKey);
        return blobClient.deleteIfExists();
    }
    
    @Override
    public boolean fileExists(String fileKey) {
        BlobClient blobClient = containerClient.getBlobClient(fileKey);
        return blobClient.exists();
    }
}
```

---

## Configuration

### application.properties (AWS S3)

```properties
# File Storage Provider Selection
file-storage.provider=s3

# AWS S3 Configuration
file-storage.s3.bucket-name=sanchalak-homework-uploads
file-storage.s3.region=us-east-1
file-storage.s3.access-key=${AWS_ACCESS_KEY_ID}
file-storage.s3.secret-key=${AWS_SECRET_ACCESS_KEY}

# Upload Settings
file-storage.upload-url-expiry-minutes=5
file-storage.download-url-expiry-minutes=15
file-storage.max-file-size-mb=10
```

### application.properties (Azure Blob Storage)

```properties
# File Storage Provider Selection
file-storage.provider=azure

# Azure Blob Storage Configuration
file-storage.azure.connection-string=${AZURE_STORAGE_CONNECTION_STRING}
file-storage.azure.container-name=sanchalak-homework-uploads

# Upload Settings
file-storage.upload-url-expiry-minutes=5
file-storage.download-url-expiry-minutes=15
file-storage.max-file-size-mb=10
```

---

## Spring Configuration

```java
package com.cm.sanchalak.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * File storage configuration.
 * Provider selection via @ConditionalOnProperty ensures only one implementation loads.
 * 
 * To switch providers:
 * 1. Change file-storage.provider in application.properties
 * 2. Update provider-specific credentials
 * 3. Restart application
 * 
 * No code changes required!
 */
@Configuration
public class FileStorageConfig {
    // Provider beans auto-configured via @ConditionalOnProperty in implementation classes
}
```

---

## Usage in Service Layer

```java
package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.PresignedUploadUrl;
import com.cm.sanchalak.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class HomeworkSubmissionService {
    
    // Inject interface, not implementation
    private final FileStorageService fileStorageService;
    
    public PresignedUploadUrl requestUploadUrl(String fileName, String contentType) {
        // Provider-agnostic - works with S3, Azure, or any future provider
        return fileStorageService.generateUploadUrl(
            fileName, 
            contentType, 
            Duration.ofMinutes(5)
        );
    }
    
    public String getDownloadUrl(String fileKey) {
        return fileStorageService.generateDownloadUrl(
            fileKey, 
            Duration.ofMinutes(15)
        );
    }
}
```

---

## Switching Providers (Zero Code Changes)

### From AWS S3 to Azure

**Step 1**: Update `application.properties`

```properties
# OLD: file-storage.provider=s3
# NEW:
file-storage.provider=azure

# Comment out S3 config
# file-storage.s3.bucket-name=...
# file-storage.s3.region=...

# Add Azure config
file-storage.azure.connection-string=DefaultEndpointsProtocol=https;AccountName=...
file-storage.azure.container-name=sanchalak-homework-uploads
```

**Step 2**: Restart application

That's it! No code changes needed. Spring Boot's `@ConditionalOnProperty` loads the correct provider.

---

## Benefits

✅ **Provider Independence**: Switch providers without code changes  
✅ **Testability**: Easy to mock `FileStorageService` interface in tests  
✅ **Extensibility**: Add Google Cloud Storage, MinIO, or local filesystem by implementing interface  
✅ **Single Responsibility**: Business logic doesn't know provider details  
✅ **Cost Flexibility**: Easily compare costs by switching providers in different environments  

---

## Future Providers

### Google Cloud Storage

```java
@Service
@ConditionalOnProperty(name = "file-storage.provider", havingValue = "gcs")
public class GcsStorageProvider implements FileStorageService {
    // Implementation using Google Cloud Storage SDK
}
```

### Local Filesystem (Dev/Test)

```java
@Service
@ConditionalOnProperty(name = "file-storage.provider", havingValue = "local")
public class LocalFileStorageProvider implements FileStorageService {
    // Implementation using java.nio.file.Files
}
```

---

**Implementation Note**: This pattern follows Dependency Inversion Principle (SOLID) - high-level modules (business logic) depend on abstractions (FileStorageService interface), not concrete implementations (S3/Azure).
