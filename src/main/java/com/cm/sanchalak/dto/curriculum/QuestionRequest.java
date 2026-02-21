package com.cm.sanchalak.dto.curriculum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class QuestionRequest {
    @NotBlank
    private String questionText;

    @NotBlank
    private String questionType;

    @NotNull
    private Integer marks;

    private List<QuestionOptionRequest> options;

    @Data
    public static class QuestionOptionRequest {
        @NotBlank
        private String optionText;

        @NotNull
        private Boolean isCorrect;
    }
}
