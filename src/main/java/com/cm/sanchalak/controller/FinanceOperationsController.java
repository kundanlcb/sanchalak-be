package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.finance.PaymentRequestDto;
import com.cm.sanchalak.dto.finance.PaymentTransactionDto;
import com.cm.sanchalak.dto.finance.StudentLedgerDto;
import com.cm.sanchalak.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceOperationsController {

    private final FinanceService financeService;

    @GetMapping("/students/{id}/ledger")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STUDENT')")
    public ResponseEntity<StudentLedgerDto> getStudentLedger(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.getStudentLedger(id));
    }

    @PostMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<PaymentTransactionDto> recordPayment(@Valid @RequestBody PaymentRequestDto dto) {
        return ResponseEntity.ok(financeService.recordPayment(dto));
    }

    @GetMapping("/receipts/{receiptNo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STUDENT')")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String receiptNo) {
        byte[] pdf = financeService.getReceiptPdf(receiptNo);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + receiptNo + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
