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
     * Preview demand bill as PDF (in-memory only, no DB save).
     * Used for in-browser PDF preview modal.
     */
    @PostMapping("/preview-pdf")
    public ResponseEntity<byte[]> previewPdf(@RequestBody DemandBillRequest request) {
        byte[] pdf = demandBillService.previewPdf(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview.pdf\"")
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

    /**
     * Get demand bill history for all students in a class.
     */
    @GetMapping("/class/{classId}/history")
    public ResponseEntity<List<DemandBillResponse>> classHistory(@PathVariable Long classId) {
        return ResponseEntity.ok(demandBillService.getClassHistory(classId));
    }
}
