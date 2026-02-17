package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.finance.PayrollRecordDto;
import com.cm.sanchalak.dto.finance.PayrollSummaryDto;

import java.util.List;

public interface PayrollService {
    List<PayrollRecordDto> getPayrollHistory();

    PayrollSummaryDto getPayrollSummary();

    void generatePayroll(String month);
}
