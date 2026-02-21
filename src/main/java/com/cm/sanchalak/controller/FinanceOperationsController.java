package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.finance.DefaulterDto;
import com.cm.sanchalak.dto.finance.PaymentRequestDto;
import com.cm.sanchalak.dto.finance.PaymentTransactionDto;
import com.cm.sanchalak.dto.finance.StudentLedgerDto;
import com.cm.sanchalak.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceOperationsController {

    private static final UUID DEFAULT_SCHOOL_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final FinanceService financeService;

    private UUID getSchoolId() {
        // TODO: Resolve from security context / tenant context
        return DEFAULT_SCHOOL_ID;
    }

    @GetMapping({ "/students/{id}/ledger", "/ledger/{id}" })
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STUDENT')")
    public ResponseEntity<StudentLedgerDto> getStudentLedger(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.getStudentLedger(id));
    }

    @PostMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<PaymentTransactionDto> recordPayment(@Valid @RequestBody PaymentRequestDto dto) {
        return ResponseEntity.ok(financeService.recordPayment(dto));
    }

    @GetMapping("/transactions/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STUDENT')")
    public ResponseEntity<List<PaymentTransactionDto>> getStudentTransactions(@PathVariable Long studentId) {
        return ResponseEntity.ok(financeService.getTransactionsByStudentId(studentId));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<List<PaymentTransactionDto>> getAllTransactions() {
        return ResponseEntity.ok(financeService.getAllTransactions(getSchoolId()));
    }

    @GetMapping("/receipts/{receiptNo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF', 'STUDENT')")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String receiptNo) {
        byte[] pdf = financeService.getReceiptPdf(receiptNo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + receiptNo + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/defaulters")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<List<DefaulterDto>> getDefaulters() {
        return ResponseEntity.ok(financeService.getDefaulters(getSchoolId()));
    }
}
