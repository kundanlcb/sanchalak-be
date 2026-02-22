package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.finance.FeeCategoryDto;
import com.cm.sanchalak.dto.finance.FeeStructureDto;
import com.cm.sanchalak.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cm.sanchalak.security.SchoolContext;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceConfigController {

    private final FinanceService financeService;

    private UUID getSchoolId() {
        return SchoolContext.getSchoolId();
    }

    @PostMapping("/categories")
    public ResponseEntity<FeeCategoryDto> createCategory(@Valid @RequestBody FeeCategoryDto dto) {
        return ResponseEntity.ok(financeService.createCategory(getSchoolId(), dto));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<FeeCategoryDto>> getAllCategories() {
        return ResponseEntity.ok(financeService.getAllCategories(getSchoolId()));
    }

    @PostMapping("/structures")
    public ResponseEntity<FeeStructureDto> createStructure(@Valid @RequestBody FeeStructureDto dto) {
        return ResponseEntity.ok(financeService.createStructure(getSchoolId(), dto));
    }

    @GetMapping("/structures")
    public ResponseEntity<List<FeeStructureDto>> getAllStructures() {
        return ResponseEntity.ok(financeService.getAllStructures(getSchoolId()));
    }

    @PostMapping("/structures/{id}/assign")
    public ResponseEntity<Void> assignStructure(@PathVariable Long id, @RequestParam Long classId) {
        financeService.assignStructureToClass(id, classId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<FeeCategoryDto> updateCategory(@PathVariable Long id,
            @Valid @RequestBody FeeCategoryDto dto) {
        return ResponseEntity.ok(financeService.updateCategory(id, dto));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        financeService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/structures/{id}")
    public ResponseEntity<FeeStructureDto> updateStructure(@PathVariable Long id,
            @Valid @RequestBody FeeStructureDto dto) {
        return ResponseEntity.ok(financeService.updateStructure(id, dto));
    }

    @DeleteMapping("/structures/{id}")
    public ResponseEntity<Void> deleteStructure(@PathVariable Long id) {
        financeService.deleteStructure(id);
        return ResponseEntity.noContent().build();
    }
}
