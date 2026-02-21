package com.cm.sanchalak.dto.curriculum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentDto {
    private Long id;
    private Long chapterId;
    private String title;
    private String contentType;
    private String contentData;
    private Integer sequenceOrder;
}
