package com.cm.sanchalak.dto.curriculum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContentRequest {
    @NotBlank
    private String title;

    // All content fields are optional — provide any combination
    private String textContent;
    private String videoUrl;
    private String pdfUrl;
    private String linkUrl;

    private Integer sequenceOrder;

    @NotNull
    private Long classId;

    @NotNull
    private Long subjectId;

    private Long chapterId;
}
