package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.analytics.CollectionTrendDto;
import com.cm.sanchalak.dto.analytics.FinancialSummaryDto;
import com.cm.sanchalak.dto.analytics.ReportCardDataDto;
import com.cm.sanchalak.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/report-card/{studentId}")
    public ResponseEntity<ReportCardDataDto> getReportCardData(
            @PathVariable Long studentId,
            @RequestParam Long termId) {
        
        ReportCardDataDto data = analyticsService.getReportCardData(studentId, termId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/finance/summary")
    public ResponseEntity<FinancialSummaryDto> getFinancialSummary() {
        return ResponseEntity.ok(analyticsService.getFinancialSummary());
    }

    @GetMapping("/finance/trend")
    public ResponseEntity<List<CollectionTrendDto>> getCollectionTrend(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getCollectionTrend(days));
    }
}
