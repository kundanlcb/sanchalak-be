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

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeCategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody FeeCategoryDto dto) {
        return ResponseEntity.ok(financeService.updateCategory(id, dto));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        financeService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/structures/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeStructureDto> updateStructure(@PathVariable Long id, @Valid @RequestBody FeeStructureDto dto) {
        return ResponseEntity.ok(financeService.updateStructure(id, dto));
    }

    @DeleteMapping("/structures/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStructure(@PathVariable Long id) {
        financeService.deleteStructure(id);
        return ResponseEntity.noContent().build();
    }
}
