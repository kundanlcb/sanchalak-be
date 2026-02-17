package com.cm.sanchalak.service.impl;

import com.cm.sanchalak.dto.finance.PayrollRecordDto;
import com.cm.sanchalak.dto.finance.PayrollSummaryDto;
import com.cm.sanchalak.service.PayrollService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayrollServiceImpl implements PayrollService {

    @Override
    public List<PayrollRecordDto> getPayrollHistory() {
        List<PayrollRecordDto> history = new ArrayList<>();

        history.add(PayrollRecordDto.builder()
                .id("PRL-001")
                .staffId("TCH-001")
                .staffName("John Doe")
                .month("January 2024")
                .basicPay(new BigDecimal("50000"))
                .allowance(new BigDecimal("5000"))
                .deduction(new BigDecimal("2000"))
                .netSalary(new BigDecimal("53000"))
                .status("Paid")
                .paidAt("2024-02-01T10:00:00Z")
                .build());

        history.add(PayrollRecordDto.builder()
                .id("PRL-002")
                .staffId("TCH-002")
                .staffName("Jane Smith")
                .month("January 2024")
                .basicPay(new BigDecimal("45000"))
                .allowance(new BigDecimal("4500"))
                .deduction(new BigDecimal("1500"))
                .netSalary(new BigDecimal("48000"))
                .status("Paid")
                .paidAt("2024-02-01T11:00:00Z")
                .build());

        return history;
    }

    @Override
    public PayrollSummaryDto getPayrollSummary() {
        return PayrollSummaryDto.builder()
                .totalPayout(new BigDecimal("101000"))
                .totalStaff(10)
                .paidStaff(8)
                .pendingStaff(2)
                .lastGenerated(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                .build();
    }

    @Override
    public void generatePayroll(String month) {
        // In a real implementation, this would iterate over active staff
        // and create records in the database.
    }
}
