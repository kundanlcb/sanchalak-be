package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.finance.PayrollRecordDto;
import com.cm.sanchalak.dto.finance.PayrollSummaryDto;
import com.cm.sanchalak.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PayrollRecordDto>> getPayrollHistory() {
        return ResponseEntity.ok(payrollService.getPayrollHistory());
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<PayrollSummaryDto> getPayrollSummary() {
        return ResponseEntity.ok(payrollService.getPayrollSummary());
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> generatePayroll(@RequestBody Map<String, String> request) {
        String month = request.get("month");
        payrollService.generatePayroll(month);
        return ResponseEntity.ok(Map.of("message", "Payroll generation started for " + month));
    }
}
