package com.cm.sanchalak.service.storage.impl;

import com.cm.sanchalak.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

/**
 * AWS S3 implementation of file storage service
 * Activated when storage.provider=s3
 */
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3", matchIfMissing = true)
public class S3StorageProvider implements FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(S3StorageProvider.class);
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String region;
    
    public S3StorageProvider(
        @Value("${storage.s3.bucket-name}") String bucketName,
        @Value("${storage.s3.region}") String region,
        @Value("${storage.s3.access-key}") String accessKey,
        @Value("${storage.s3.secret-key}") String secretKey) {
        
        this.bucketName = bucketName;
        this.region = region;
        
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        this.s3Client = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
        
        this.s3Presigner = S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
        
        logger.info("S3StorageProvider initialized with bucket: {}, region: {}", bucketName, region);
    }
    
    @Override
    public String generateUploadUrl(String objectKey, String contentType, int expiryMinutes) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .contentType(contentType)
            .build();
        
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(expiryMinutes))
            .putObjectRequest(objectRequest)
            .build();
        
        String url = s3Presigner.presignPutObject(presignRequest).url().toString();
        logger.debug("Generated S3 upload URL for key: {}", objectKey);
        return url;
    }
    
    @Override
    public String generateDownloadUrl(String objectKey, int expiryMinutes) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();
        
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(expiryMinutes))
            .getObjectRequest(objectRequest)
            .build();
        
        String url = s3Presigner.presignGetObject(presignRequest).url().toString();
        logger.debug("Generated S3 download URL for key: {}", objectKey);
        return url;
    }
    
    @Override
    public void deleteFile(String objectKey) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();
        
        s3Client.deleteObject(deleteRequest);
        logger.info("Deleted S3 object: {}", objectKey);
    }
    
    @Override
    public String getPublicUrl(String objectKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);
    }
}
