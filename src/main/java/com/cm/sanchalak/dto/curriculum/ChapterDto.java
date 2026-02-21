package com.cm.sanchalak.dto.curriculum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChapterDto {
    private Long id;
    private Long classId;
    private Long subjectId;
    private String name;
    private String description;
    private Integer sequenceOrder;
}
