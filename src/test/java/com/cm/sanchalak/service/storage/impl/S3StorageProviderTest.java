package com.cm.sanchalak.service.storage.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageProviderTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3StorageProvider s3StorageProvider;
    private final String bucketName = "test-bucket";
    private final String region = "us-east-1";

    @BeforeEach
    void setUp() {
        s3StorageProvider = new S3StorageProvider(bucketName, region, s3Client, s3Presigner);
    }

    @Test
    void testGenerateUploadUrl() throws MalformedURLException {
        // Arrange
        String objectKey = "homework/hw1.pdf";
        String contentType = "application/pdf";
        int expiryMinutes = 15;
        String expectedUrl = "https://s3.amazonaws.com/test-bucket/homework/hw1.pdf?signature=123";

        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url()).thenReturn(new URL(expectedUrl));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedRequest);

        // Act
        String url = s3StorageProvider.generateUploadUrl(objectKey, contentType, expiryMinutes);

        // Assert
        assertEquals(expectedUrl, url);
        
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
    }

    @Test
    void testGenerateDownloadUrl() throws MalformedURLException {
        // Arrange
        String objectKey = "homework/hw1.pdf";
        int expiryMinutes = 60;
        String expectedUrl = "https://s3.amazonaws.com/test-bucket/homework/hw1.pdf?signature=abc";

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(new URL(expectedUrl));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        // Act
        String url = s3StorageProvider.generateDownloadUrl(objectKey, expiryMinutes);

        // Assert
        assertEquals(expectedUrl, url);
    }

    @Test
    void testDeleteFile() {
        // Arrange
        String objectKey = "homework/hw1.pdf";

        // Act
        s3StorageProvider.deleteFile(objectKey);

        // Assert
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        
        DeleteObjectRequest request = captor.getValue();
        assertEquals(bucketName, request.bucket());
        assertEquals(objectKey, request.key());
    }
}
