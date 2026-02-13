package com.cm.sanchalak.service.storage.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureBlobStorageProviderTest {

    @Mock
    private BlobServiceClient blobServiceClient;
    @Mock
    private BlobContainerClient containerClient;
    @Mock
    private BlobClient blobClient;

    private AzureBlobStorageProvider azureBlobStorageProvider;
    private final String containerName = "test-container";

    @BeforeEach
    void setUp() {
        azureBlobStorageProvider = new AzureBlobStorageProvider(containerName, blobServiceClient, containerClient);
    }

    @Test
    void testGenerateUploadUrl() {
        // Arrange
        String objectKey = "homework/hw1.pdf";
        String contentType = "application/pdf";
        int expiryMinutes = 15;
        String blobUrl = "https://azure.blob.core.windows.net/test-container/homework/hw1.pdf";
        String sasToken = "sv=2020-08-04&ss=b&srt=o&sp=rwdlacx&se=2021-08-04T00:00:00Z";
        String expectedUrl = blobUrl + "?" + sasToken;

        when(containerClient.getBlobClient(objectKey)).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn(blobUrl);
        when(blobClient.generateSas(any(BlobServiceSasSignatureValues.class))).thenReturn(sasToken);

        // Act
        String url = azureBlobStorageProvider.generateUploadUrl(objectKey, contentType, expiryMinutes);

        // Assert
        assertEquals(expectedUrl, url);
        verify(containerClient).getBlobClient(objectKey);
        verify(blobClient).generateSas(any(BlobServiceSasSignatureValues.class));
    }

    @Test
    void testGenerateDownloadUrl() {
        // Arrange
        String objectKey = "homework/hw1.pdf";
        int expiryMinutes = 60;
        String blobUrl = "https://azure.blob.core.windows.net/test-container/homework/hw1.pdf";
        String sasToken = "sv=2020-08-04&ss=b&srt=o&sp=r&se=2021-08-04T00:00:00Z";
        String expectedUrl = blobUrl + "?" + sasToken;

        when(containerClient.getBlobClient(objectKey)).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn(blobUrl);
        when(blobClient.generateSas(any(BlobServiceSasSignatureValues.class))).thenReturn(sasToken);

        // Act
        String url = azureBlobStorageProvider.generateDownloadUrl(objectKey, expiryMinutes);

        // Assert
        assertEquals(expectedUrl, url);
    }

    @Test
    void testDeleteFile() {
        // Arrange
        String objectKey = "homework/hw1.pdf";
        when(containerClient.getBlobClient(objectKey)).thenReturn(blobClient);

        // Act
        azureBlobStorageProvider.deleteFile(objectKey);

        // Assert
        verify(containerClient).getBlobClient(objectKey);
        verify(blobClient).delete();
    }
}
