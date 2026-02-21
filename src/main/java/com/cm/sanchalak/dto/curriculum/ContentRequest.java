package com.cm.sanchalak.dto.curriculum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContentRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String contentType;

    @NotBlank
    private String contentData;

    @NotNull
    private Integer sequenceOrder;
}
