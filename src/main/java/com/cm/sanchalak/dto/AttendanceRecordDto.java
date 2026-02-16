package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecordDto {
    private Long id;

    @JsonProperty("studentId")
    private Long studentId;
    @JsonProperty("studentID")
    private String studentIdStr;

    @JsonProperty("classId")
    private Long classId;
    @JsonProperty("classID")
    private String classIdStr;

    private LocalDate date;
    private AttendanceStatus status;
    private String remarks;
    private String markedBy;
    private LocalDateTime markedDate;
    private boolean isModified;
}
