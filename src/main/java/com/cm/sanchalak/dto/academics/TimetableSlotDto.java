package com.cm.sanchalak.dto.academics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSlotDto {
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isBreak;
    private Integer orderIndex;
}
