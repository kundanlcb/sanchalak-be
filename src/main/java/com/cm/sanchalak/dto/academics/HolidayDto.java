package com.cm.sanchalak.dto.academics;

import com.cm.sanchalak.entity.academics.HolidayType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayDto {
    private Long id;
    private String tenantId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private HolidayType type;
    private boolean applicableToStudents;
    private boolean applicableToStaff;
    private String academicYear;
}
