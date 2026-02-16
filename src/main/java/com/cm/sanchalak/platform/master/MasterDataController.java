package com.cm.sanchalak.platform.master;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platform/v1/masters")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataService masterDataService;

    // TODO: Add endpoint to list domains if needed

    @GetMapping("/domains/{domainCode}/values")
    public ResponseEntity<List<MasterValueDto>> getValues(
            @PathVariable String domainCode,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(masterDataService.getValues(domainCode, activeOnly));
    }

    // Admin only endpoint to add values
    @PostMapping("/domains/{domainCode}/values")
    public ResponseEntity<Void> addValue(
            @PathVariable String domainCode,
            @RequestBody MasterValueDto dto) {
        masterDataService.createValue(domainCode, dto);
        return ResponseEntity.status(201).build();
    }
}
