package com.cm.sanchalak.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollSummaryDto {
    private BigDecimal totalPayout;
    private int totalStaff;
    private int paidStaff;
    private int pendingStaff;
    private String lastGenerated;
}
