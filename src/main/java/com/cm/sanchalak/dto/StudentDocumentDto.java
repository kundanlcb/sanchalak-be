package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDocumentDto {
    private Long id;
    private Long studentId;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private String description;
    private LocalDateTime uploadedAt;
}
