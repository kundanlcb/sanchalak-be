package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.finance.DemandBillRequest;
import com.cm.sanchalak.dto.finance.DemandBillResponse;
import com.cm.sanchalak.service.DemandBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees/demand-bill")
@RequiredArgsConstructor
public class DemandBillController {

    private final DemandBillService demandBillService;

    /**
     * Preview demand bills for a class (or all school if classId is null).
     * Used to show admin the bill data before generating PDF.
     */
    @PostMapping("/preview")
    public ResponseEntity<List<DemandBillResponse>> preview(@RequestBody DemandBillRequest request) {
        return ResponseEntity.ok(demandBillService.preview(request));
    }

    /**
     * Generate and download demand bill PDF (2-up A4 layout).
     */
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@RequestBody DemandBillRequest request) {
        byte[] pdf = demandBillService.generatePdf(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"demand-bills.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Get demand bill history for a specific student.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<DemandBillResponse>> studentHistory(@PathVariable Long studentId) {
        return ResponseEntity.ok(demandBillService.getStudentHistory(studentId));
    }
}
