package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.platform.subscription.SchoolSubscription;
import com.cm.sanchalak.platform.subscription.SubscriptionPlan;
import com.cm.sanchalak.platform.subscription.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cm.sanchalak.platform.school.SchoolFeatureEntitlementService;
import com.cm.sanchalak.platform.subscription.SubscriptionPlanRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;
    private final SchoolFeatureEntitlementService schoolFeatureEntitlementService;

    public SubscriptionController(SubscriptionService service,
            SchoolFeatureEntitlementService schoolFeatureEntitlementService) {
        this.service = service;
        this.schoolFeatureEntitlementService = schoolFeatureEntitlementService;
    }

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        return ResponseEntity.ok(service.getAllPlans());
    }

    @PostMapping("/plans")
    public ResponseEntity<SubscriptionPlan> createPlan(@RequestBody SubscriptionPlanRequest request) {
        return ResponseEntity.ok(service.createPlan(request));
    }

    @PostMapping("/assign/{schoolId}")
    public ResponseEntity<SchoolSubscription> assignPlan(@PathVariable UUID schoolId, @RequestParam UUID planId) {
        SchoolSubscription subscription = service.assignPlan(schoolId, planId);
        schoolFeatureEntitlementService.seedFeaturesFromPlan(schoolId, planId);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/active/{schoolId}")
    public ResponseEntity<SchoolSubscription> getActiveSubscription(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(service.getActiveSubscription(schoolId));
    }
}
