package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for presigned URL response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlDto {
    
    private String uploadUrl;
    
    private String objectKey;
    
    private int expiryMinutes;
    
    private String instructions;
}
