package com.cm.sanchalak.dto.curriculum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChapterRequest {
    @NotNull
    private Long classId;

    @NotNull
    private Long subjectId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Integer sequenceOrder;
}
