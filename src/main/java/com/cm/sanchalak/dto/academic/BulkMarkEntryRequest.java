package com.cm.sanchalak.dto.academic;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BulkMarkEntryRequest {

    @NotNull
    private Long examTermId;

    @NotNull
    private Long classId;

    @NotNull
    private Long subjectId;

    @NotNull
    private List<StudentMarkEntryDto> marks;

    public Long getExamTermId() {
        return examTermId;
    }

    public void setExamTermId(Long examTermId) {
        this.examTermId = examTermId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public List<StudentMarkEntryDto> getMarks() {
        return marks;
    }

    public void setMarks(List<StudentMarkEntryDto> marks) {
        this.marks = marks;
    }
}
