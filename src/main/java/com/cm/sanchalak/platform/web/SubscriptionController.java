package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.platform.subscription.SchoolSubscription;
import com.cm.sanchalak.platform.subscription.SubscriptionPlan;
import com.cm.sanchalak.platform.subscription.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        return ResponseEntity.ok(service.getAllPlans());
    }

    @PostMapping("/plans")
    public ResponseEntity<SubscriptionPlan> createPlan(@RequestBody SubscriptionPlan plan) {
        return ResponseEntity.ok(service.createPlan(plan));
    }

    @PostMapping("/assign/{schoolId}")
    public ResponseEntity<SchoolSubscription> assignPlan(@PathVariable UUID schoolId, @RequestParam UUID planId) {
        return ResponseEntity.ok(service.assignPlan(schoolId, planId));
    }

    @GetMapping("/active/{schoolId}")
    public ResponseEntity<SchoolSubscription> getActiveSubscription(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(service.getActiveSubscription(schoolId));
    }
}
