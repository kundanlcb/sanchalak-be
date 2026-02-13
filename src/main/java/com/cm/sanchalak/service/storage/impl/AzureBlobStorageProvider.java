package com.cm.sanchalak.service.storage.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.cm.sanchalak.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration
;
import java.time.OffsetDateTime;

/**
 * Azure Blob Storage implementation of file storage service
 * Activated when storage.provider=azure
 */
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "azure")
public class AzureBlobStorageProvider implements FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureBlobStorageProvider.class);
    
    private final BlobServiceClient blobServiceClient;
    private final BlobContainerClient containerClient;
    private final String containerName;
    
    public AzureBlobStorageProvider(
        @Value("${storage.azure.connection-string}") String connectionString,
        @Value("${storage.azure.container-name}") String containerName) {
        
        this.containerName = containerName;
        
        this.blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
        
        logger.info("AzureBlobStorageProvider initialized with container: {}", containerName);
    }
    
    @Override
    public String generateUploadUrl(String objectKey, String contentType, int expiryMinutes) {
        BlobClient blobClient = containerClient.getBlobClient(objectKey);
        
        BlobSasPermission permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true);
        
        OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(expiryMinutes);
        
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
            .setContentType(contentType);
        
        String sasToken = blobClient.generateSas(sasValues);
        String url = blobClient.getBlobUrl() + "?" + sasToken;
        
        logger.debug("Generated Azure Blob upload URL for key: {}", objectKey);
        return url;
    }
    
    @Override
    public String generateDownloadUrl(String objectKey, int expiryMinutes) {
        BlobClient blobClient = containerClient.getBlobClient(objectKey);
        
        BlobSasPermission permissions = new BlobSasPermission()
            .setReadPermission(true);
        
        OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(expiryMinutes);
        
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        
        String sasToken = blobClient.generateSas(sasValues);
        String url = blobClient.getBlobUrl() + "?" + sasToken;
        
        logger.debug("Generated Azure Blob download URL for key: {}", objectKey);
        return url;
    }
    
    @Override
    public void deleteFile(String objectKey) {
        BlobClient blobClient = containerClient.getBlobClient(objectKey);
        blobClient.delete();
        
        logger.info("Deleted Azure Blob: {}", objectKey);
    }
    
    @Override
    public String getPublicUrl(String objectKey) {
        BlobClient blobClient = containerClient.getBlobClient(objectKey);
        return blobClient.getBlobUrl();
    }
}
