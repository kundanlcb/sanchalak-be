package com.cm.sanchalak.dto.academic;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class ExamScheduleRequest {
    @NotNull
    private Long examTermId;

    @NotNull
    private Long classId;

    @NotNull
    private Long subjectId;

    @NotNull
    private LocalDate examDate;

    @Positive
    private Integer maxMarks;

    public Long getExamTermId() { return examTermId; }
    public void setExamTermId(Long examTermId) { this.examTermId = examTermId; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }
}
