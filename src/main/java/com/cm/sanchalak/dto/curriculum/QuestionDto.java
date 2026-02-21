package com.cm.sanchalak.dto.curriculum;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuestionDto {
    private Long id;
    private Long chapterId;
    private String questionText;
    private String questionType;
    private Integer marks;
    private List<QuestionOptionDto> options;

    @Data
    @Builder
    public static class QuestionOptionDto {
        private Long id;
        private String optionText;
        private Boolean isCorrect;
    }
}
