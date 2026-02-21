package com.cm.sanchalak.dto.academic;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ExamQuestionRequest {
    @NotNull
    private Long questionId;

    @Positive
    private Integer marks;

    private Integer sequenceOrder;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
}
