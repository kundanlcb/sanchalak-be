package com.cm.sanchalak.platform.subscription;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final SchoolSubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionPlanRepository planRepository,
            SchoolSubscriptionRepository subscriptionRepository) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<SubscriptionPlan> getAllPlans() {
        return planRepository.findAll();
    }

    @Transactional
    public SubscriptionPlan createPlan(SubscriptionPlan plan) {
        return planRepository.save(plan);
    }

    @Transactional
    public SchoolSubscription assignPlan(UUID schoolId, UUID planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        // Deactivate existing active subscriptions
        subscriptionRepository.findBySchoolId(schoolId).forEach(sub -> {
            if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                sub.setStatus(SubscriptionStatus.CANCELLED); // or EXPIRED
                subscriptionRepository.save(sub);
            }
        });

        SchoolSubscription subscription = new SchoolSubscription();
        subscription.setSchoolId(schoolId);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusMonths(plan.getDurationMonths()));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        return subscriptionRepository.save(subscription);
    }

    public SchoolSubscription getActiveSubscription(UUID schoolId) {
        // Simple strategy getting the first active one or creating a query
        return subscriptionRepository.findBySchoolIdAndStatus(schoolId, SubscriptionStatus.ACTIVE)
                .orElse(null);
    }
}
