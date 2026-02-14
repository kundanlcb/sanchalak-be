package com.cm.sanchalak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDocumentRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Document Type is required")
    private String documentType; // AADHAR, PAN, CERTIFICATE, etc.

    @NotBlank(message = "File Name is required")
    private String fileName;

    @NotBlank(message = "File URL is required")
    private String fileUrl; // The S3 key or full URL

    @NotBlank(message = "MIME Type is required")
    private String mimeType;

    private Long fileSize; // Optional

    private String description;
}
