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
public class PayrollRecordDto {
    private String id;
    private String staffId;
    private String staffName;
    private String month; // e.g. "January 2024"
    private BigDecimal basicPay;
    private BigDecimal allowance;
    private BigDecimal deduction;
    private BigDecimal netSalary;
    private String status; // e.g. "Paid", "Pending"
    private String paidAt; // ISO date string
}
