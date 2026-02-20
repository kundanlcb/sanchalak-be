package com.cm.sanchalak.platform.school;

import com.cm.sanchalak.platform.school.dto.SchoolFeatureStateDto;
import com.cm.sanchalak.platform.subscription.Feature;
import com.cm.sanchalak.platform.subscription.FeatureRepository;
import com.cm.sanchalak.platform.subscription.SchoolSubscription;
import com.cm.sanchalak.platform.subscription.SubscriptionPlan;
import com.cm.sanchalak.platform.subscription.SubscriptionPlanRepository;
import com.cm.sanchalak.platform.subscription.SubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SchoolFeatureEntitlementService {

    private final SchoolRepository schoolRepository;
    private final FeatureRepository featureRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionService subscriptionService;
    private final SchoolFeatureEntitlementRepository entitlementRepository;

    public SchoolFeatureEntitlementService(
            SchoolRepository schoolRepository,
            FeatureRepository featureRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            SubscriptionService subscriptionService,
            SchoolFeatureEntitlementRepository entitlementRepository) {
        this.schoolRepository = schoolRepository;
        this.featureRepository = featureRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionService = subscriptionService;
        this.entitlementRepository = entitlementRepository;
    }

    @Transactional
    public void seedFeaturesFromPlan(UUID schoolId, UUID planId) {
        validateSchool(schoolId);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));

        if (plan.getFeatures() == null || plan.getFeatures().isEmpty()) {
            return;
        }

        Map<UUID, SchoolFeatureEntitlement> existing = entitlementRepository.findBySchoolId(schoolId).stream()
                .collect(Collectors.toMap(e -> e.getFeature().getId(), Function.identity()));

        for (Feature feature : plan.getFeatures()) {
            SchoolFeatureEntitlement entitlement = existing.get(feature.getId());
            if (entitlement == null) {
                entitlement = new SchoolFeatureEntitlement();
                entitlement.setSchoolId(schoolId);
                entitlement.setFeature(feature);
            }
            entitlement.setEnabled(true);
            entitlement.setSourcePlanId(planId);
            entitlementRepository.save(entitlement);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getEnabledFeatureCodes(UUID schoolId) {
        return entitlementRepository.findBySchoolIdAndEnabledTrue(schoolId).stream()
                .map(e -> e.getFeature().getCode())
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SchoolFeatureStateDto> getSchoolFeatureStates(UUID schoolId) {
        validateSchool(schoolId);

        List<Feature> allFeatures = featureRepository.findAll();
        Map<UUID, SchoolFeatureEntitlement> entitlementByFeature = entitlementRepository.findBySchoolId(schoolId)
                .stream()
                .collect(Collectors.toMap(e -> e.getFeature().getId(), Function.identity()));

        Set<UUID> activePlanFeatureIds = getActivePlanFeatureIds(schoolId);

        return allFeatures.stream()
                .map(feature -> buildState(feature, entitlementByFeature.get(feature.getId()), activePlanFeatureIds))
                .sorted(Comparator.comparing(SchoolFeatureStateDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public SchoolFeatureStateDto setFeatureEnabled(UUID schoolId, UUID featureId, boolean enabled) {
        validateSchool(schoolId);

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new RuntimeException("Feature not found: " + featureId));

        SchoolFeatureEntitlement entitlement = entitlementRepository.findBySchoolIdAndFeature_Id(schoolId, featureId)
                .orElse(null);

        if (enabled) {
            if (entitlement == null) {
                entitlement = new SchoolFeatureEntitlement();
                entitlement.setSchoolId(schoolId);
                entitlement.setFeature(feature);
            }
            entitlement.setEnabled(true);
            if (entitlement.getSourcePlanId() == null) {
                entitlement.setSourcePlanId(getActivePlanId(schoolId));
            }
            entitlement = entitlementRepository.save(entitlement);
        } else if (entitlement != null) {
            entitlement.setEnabled(false);
            entitlement = entitlementRepository.save(entitlement);
        }

        Set<UUID> activePlanFeatureIds = getActivePlanFeatureIds(schoolId);
        return buildState(feature, entitlement, activePlanFeatureIds);
    }

    private SchoolFeatureStateDto buildState(
            Feature feature,
            SchoolFeatureEntitlement entitlement,
            Set<UUID> activePlanFeatureIds) {
        boolean assigned = entitlement != null;
        boolean featureEnabled = assigned && entitlement.isEnabled();

        return new SchoolFeatureStateDto(
                feature.getId(),
                feature.getCode(),
                feature.getName(),
                feature.getDescription(),
                activePlanFeatureIds.contains(feature.getId()),
                assigned,
                featureEnabled);
    }

    private Set<UUID> getActivePlanFeatureIds(UUID schoolId) {
        SchoolSubscription activeSubscription = subscriptionService.getActiveSubscription(schoolId);
        if (activeSubscription == null || activeSubscription.getPlan() == null ||
                activeSubscription.getPlan().getFeatures() == null) {
            return Set.of();
        }

        return activeSubscription.getPlan().getFeatures().stream()
                .map(Feature::getId)
                .collect(Collectors.toSet());
    }

    private UUID getActivePlanId(UUID schoolId) {
        SchoolSubscription activeSubscription = subscriptionService.getActiveSubscription(schoolId);
        if (activeSubscription == null || activeSubscription.getPlan() == null) {
            return null;
        }
        return activeSubscription.getPlan().getId();
    }

    private void validateSchool(UUID schoolId) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new RuntimeException("School not found: " + schoolId);
        }
    }
}
