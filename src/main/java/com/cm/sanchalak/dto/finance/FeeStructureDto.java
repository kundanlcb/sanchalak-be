package com.cm.sanchalak.dto.finance;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FeeStructureDto {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Academic year is required")
    private String academicYear;
    @NotBlank(message = "Frequency is required")
    private String frequency;
    private BigDecimal lateFeeAmount;
    private Integer gracePeriodDays;
    private List<FeeStructureItemDto> items;
}
