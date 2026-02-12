package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassAttendanceSheetDto {
    private Long classId;
    private LocalDate date;
    private int presentCount;
    private int absentCount;
    private int totalCount;
    private List<AttendanceRecordDto> students;
}
