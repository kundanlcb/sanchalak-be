package com.cm.sanchalak.service.storage;

/**
 * File storage service interface
 * Abstraction over cloud storage providers (AWS S3, Azure Blob Storage)
 */
public interface FileStorageService {
    
    /**
     * Generate a presigned URL for uploading a file
     * 
     * @param objectKey The unique key/path for the file
     * @param contentType MIME type of the file
     * @param expiryMinutes How long the URL should be valid
     * @return Presigned URL for upload
     */
    String generateUploadUrl(String objectKey, String contentType, int expiryMinutes);
    
    /**
     * Generate a presigned URL for downloading a file
     * 
     * @param objectKey The unique key/path for the file
     * @param expiryMinutes How long the URL should be valid
     * @return Presigned URL for download
     */
    String generateDownloadUrl(String objectKey, int expiryMinutes);
    
    /**
     * Delete a file from storage
     * 
     * @param objectKey The unique key/path for the file
     */
    void deleteFile(String objectKey);
    
    /**
     * Get the public URL for a file (if bucket is public)
     * 
     * @param objectKey The unique key/path for the file
     * @return Public URL
     */
    String getPublicUrl(String objectKey);
}
