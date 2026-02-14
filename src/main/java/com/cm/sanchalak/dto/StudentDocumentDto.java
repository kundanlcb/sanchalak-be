package com.cm.sanchalak.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentDocumentDto {
    private Long id;
    private Long studentId;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private String description;
    private LocalDateTime uploadedAt;
}
