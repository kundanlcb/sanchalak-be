package com.cm.sanchalak.dto.academic;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class MarkEntryRequest {
    @NotNull
    private Long examScheduleId;

    @NotNull
    private Long studentId;

    @NotNull
    @PositiveOrZero
    private Double marksObtained;

    private String remarks;

    public Long getExamScheduleId() { return examScheduleId; }
    public void setExamScheduleId(Long examScheduleId) { this.examScheduleId = examScheduleId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
