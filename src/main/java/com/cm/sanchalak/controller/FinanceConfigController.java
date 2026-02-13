package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.finance.FeeCategoryDto;
import com.cm.sanchalak.dto.finance.FeeStructureDto;
import com.cm.sanchalak.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceConfigController {

    private final FinanceService financeService;

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeCategoryDto> createCategory(@Valid @RequestBody FeeCategoryDto dto) {
        return ResponseEntity.ok(financeService.createCategory(dto));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<List<FeeCategoryDto>> getAllCategories() {
        return ResponseEntity.ok(financeService.getAllCategories());
    }

    @PostMapping("/structures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeStructureDto> createStructure(@Valid @RequestBody FeeStructureDto dto) {
        return ResponseEntity.ok(financeService.createStructure(dto));
    }

    @GetMapping("/structures")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_STAFF')")
    public ResponseEntity<List<FeeStructureDto>> getAllStructures() {
        return ResponseEntity.ok(financeService.getAllStructures());
    }

    @PostMapping("/structures/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignStructure(@PathVariable Long id, @RequestParam Long classId) {
        financeService.assignStructureToClass(id, classId);
        return ResponseEntity.ok().build();
    }
}
