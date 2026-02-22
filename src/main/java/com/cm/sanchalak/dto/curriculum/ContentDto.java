package com.cm.sanchalak.dto.curriculum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentDto {
    private Long id;
    private Long chapterId;
    private Long classId;
    private Long subjectId;
    private String title;
    private String textContent;
    private String videoUrl;
    private String pdfUrl;
    private String linkUrl;
    private Integer sequenceOrder;
}
