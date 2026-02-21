package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.dto.finance.FeeCategoryDto;
import com.cm.sanchalak.dto.finance.FeeStructureDto;
import com.cm.sanchalak.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/schools/{schoolId}/finance")
public class PlatformFinanceController {

    private final FinanceService financeService;

    public PlatformFinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    // Fee Configuration - Categories
    @PostMapping("/categories")
    public ResponseEntity<FeeCategoryDto> createCategory(@PathVariable UUID schoolId,
            @RequestBody FeeCategoryDto dto) {
        return ResponseEntity.ok(financeService.createCategory(schoolId, dto));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<FeeCategoryDto>> getCategories(
            @PathVariable UUID schoolId) {
        return ResponseEntity.ok(financeService.getAllCategories(schoolId));
    }

    // Fee Configuration - Structures
    @PostMapping("/structures")
    public ResponseEntity<FeeStructureDto> createStructure(@PathVariable UUID schoolId,
            @RequestBody FeeStructureDto dto) {
        return ResponseEntity.ok(financeService.createStructure(schoolId, dto));
    }

    @GetMapping("/structures")
    public ResponseEntity<List<FeeStructureDto>> getStructures(
            @PathVariable UUID schoolId) {
        return ResponseEntity.ok(financeService.getAllStructures(schoolId));
    }

    // Billing Readiness - Trigger Invoice Generation
    @PostMapping("/invoices/generate")
    public ResponseEntity<Void> generateInvoices(@PathVariable UUID schoolId) {
        financeService.generateInvoicesForSchool(schoolId);
        return ResponseEntity.ok().build();
    }
}
