package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.platform.subscription.Feature;
import com.cm.sanchalak.platform.subscription.FeatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/features")
public class FeatureController {

    private final FeatureService service;

    public FeatureController(FeatureService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Feature>> getAllFeatures() {
        return ResponseEntity.ok(service.getAllFeatures());
    }

    @PostMapping
    public ResponseEntity<Feature> createFeature(@RequestBody Feature feature) {
        return ResponseEntity.ok(service.createFeature(feature));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeature(@PathVariable UUID id) {
        service.deleteFeature(id);
        return ResponseEntity.ok().build();
    }
}
