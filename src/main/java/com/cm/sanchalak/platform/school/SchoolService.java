package com.cm.sanchalak.platform.school;

import com.cm.sanchalak.entity.AcademicYear;
import com.cm.sanchalak.platform.academic.AcademicYearRepository;
import com.cm.sanchalak.platform.onboarding.OnboardingStatus;
import com.cm.sanchalak.platform.onboarding.SchoolOnboardingRequest;
import com.cm.sanchalak.platform.onboarding.BootstrapAdminRequest;
import com.cm.sanchalak.platform.onboarding.BootstrapAdminService;
import com.cm.sanchalak.platform.subscription.SchoolSubscriptionRepository;
import com.cm.sanchalak.platform.subscription.SubscriptionStatus;
import com.cm.sanchalak.platform.subscription.SubscriptionService;
import com.cm.sanchalak.exception.DuplicateResourceException;
import com.cm.sanchalak.exception.ResourceNotFoundException;
import com.cm.sanchalak.exception.AppException;
import com.cm.sanchalak.dto.UserProfileDto;
import com.cm.sanchalak.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolUserRepository schoolUserRepository;
    private final SchoolSubscriptionRepository subscriptionRepository;
    private final BootstrapAdminService bootstrapAdminService;
    private final SubscriptionService subscriptionService;
    private final SchoolFeatureEntitlementService schoolFeatureEntitlementService;
    private final UserRepository userRepository;

    public SchoolService(SchoolRepository schoolRepository,
            AcademicYearRepository academicYearRepository,
            SchoolUserRepository schoolUserRepository,
            SchoolSubscriptionRepository subscriptionRepository,
            BootstrapAdminService bootstrapAdminService,
            SubscriptionService subscriptionService,
            SchoolFeatureEntitlementService schoolFeatureEntitlementService,
            UserRepository userRepository) {
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.schoolUserRepository = schoolUserRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.bootstrapAdminService = bootstrapAdminService;
        this.subscriptionService = subscriptionService;
        this.schoolFeatureEntitlementService = schoolFeatureEntitlementService;
        this.userRepository = userRepository;
    }

    public OnboardingStatus getOnboardingStatus(UUID schoolId) {
        School school = getSchoolById(schoolId);

        boolean profileComplete = isSchoolProfileComplete(school);
        boolean academicYearCreated = !academicYearRepository.findBySchoolId(schoolId).isEmpty();
        boolean adminUserInvited = schoolUserRepository.existsBySchoolId(schoolId);
        boolean subscriptionActive = subscriptionRepository.findBySchoolIdAndStatus(schoolId, SubscriptionStatus.ACTIVE)
                .isPresent();

        return OnboardingStatus.builder()
                .profileComplete(profileComplete)
                .academicYearCreated(academicYearCreated)
                .adminUserInvited(adminUserInvited)
                .subscriptionActive(subscriptionActive)
                .build();
    }

    public List<UserProfileDto> getSchoolAdmins(UUID schoolId) {
        List<SchoolUser> schoolUsers = schoolUserRepository.findBySchoolId(schoolId);
        List<UUID> userIds = schoolUsers.stream().map(SchoolUser::getUserId).toList();
        return userRepository.findAllById(userIds).stream()
                .map(u -> UserProfileDto.builder()
                        .userId(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .mobileNumber(u.getMobileNumber())
                        .role(u.getRoles().isEmpty() ? "ADMIN" : u.getRoles().iterator().next().getName().name())
                        .build())
                .toList();
    }

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    public School getSchoolById(UUID schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School", "id", schoolId));
    }

    @Transactional
    public School createSchool(School school) {
        if (!hasText(school.getSchoolCode())) {
            throw new IllegalArgumentException("schoolCode is required");
        }
        if (!hasText(school.getName())) {
            throw new IllegalArgumentException("name is required");
        }

        school.setSchoolCode(school.getSchoolCode().trim().toUpperCase());
        school.setName(school.getName().trim());

        if (schoolRepository.existsBySchoolCode(school.getSchoolCode())) {
            throw new DuplicateResourceException("School", "schoolCode", school.getSchoolCode());
        }

        school.setStatus(SchoolStatus.DRAFT);
        // BaseEntity fields managed by JPA auditing if enabled, else set manually
        // school.setCreatedAt(Instant.now());
        // school.setUpdatedAt(Instant.now());

        return schoolRepository.save(school);
    }

    @Transactional
    public School transitionStatus(UUID schoolId, SchoolStatus newStatus) {
        School school = getSchoolById(schoolId);
        // Add validation logic for state transitions here if needed
        school.setStatus(newStatus);
        return schoolRepository.save(school);
    }

    @Transactional
    public School updateSchool(UUID schoolId, School updatedSchool) {
        School existingSchool = getSchoolById(schoolId);

        if (updatedSchool.getSchoolCode() != null) {
            String schoolCode = updatedSchool.getSchoolCode().trim().toUpperCase();
            if (!hasText(schoolCode)) {
                throw new IllegalArgumentException("schoolCode cannot be blank");
            }
            if (schoolRepository.existsBySchoolCodeAndIdNot(schoolCode, schoolId)) {
                throw new DuplicateResourceException("School", "schoolCode", schoolCode);
            }
            existingSchool.setSchoolCode(schoolCode);
        }
        if (updatedSchool.getName() != null) {
            String name = updatedSchool.getName().trim();
            if (!hasText(name)) {
                throw new IllegalArgumentException("name cannot be blank");
            }
            existingSchool.setName(name);
        }
        if (updatedSchool.getBoard() != null) {
            existingSchool.setBoard(toNullIfBlank(updatedSchool.getBoard()));
        }
        if (updatedSchool.getContactInfo() != null) {
            existingSchool.setContactInfo(updatedSchool.getContactInfo());
        }
        if (updatedSchool.getRegistrationNumber() != null) {
            existingSchool.setRegistrationNumber(toNullIfBlank(updatedSchool.getRegistrationNumber()));
        }
        if (updatedSchool.getTimezone() != null) {
            existingSchool.setTimezone(toNullIfBlank(updatedSchool.getTimezone()));
        }

        return schoolRepository.save(existingSchool);
    }

    @Transactional
    public School onboardSchool(SchoolOnboardingRequest request) {
        validateOnboardingRequest(request);
        String schoolCode = request.getSchoolCode().trim().toUpperCase();

        // 1. Create School
        School school = new School();
        school.setName(request.getSchoolName());
        school.setSchoolCode(schoolCode);
        school.setBoard(request.getBoard());
        school.setRegistrationNumber(request.getRegistrationNumber());
        school.setTimezone(request.getTimezone());
        school.setContactInfo(request.getContactInfo());
        school.setStatus(SchoolStatus.ACTIVE);

        School savedSchool = schoolRepository.save(school);

        // 2. Bootstrap Admin
        BootstrapAdminRequest adminRequest = BootstrapAdminRequest.builder()
                .name(request.getAdminName())
                .email(request.getAdminEmail())
                .password(request.getAdminPassword() != null ? request.getAdminPassword() : request.getAdminEmail())
                .mobileNumber(request.getAdminMobile())
                .build();

        bootstrapAdminService.bootstrapAdmin(savedSchool.getId(), adminRequest);

        // 3. Create Academic Year
        AcademicYear academicYear = AcademicYear.builder()
                .schoolId(savedSchool.getId())
                .name(request.getAcademicYearName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isCurrent(true)
                .build();

        academicYearRepository.save(academicYear);

        // 4. Assign Subscription
        subscriptionService.assignPlan(savedSchool.getId(), request.getPlanId());

        // 5. Snapshot plan features into school entitlements
        schoolFeatureEntitlementService.seedFeaturesFromPlan(savedSchool.getId(), request.getPlanId());

        return savedSchool;
    }

    @Transactional
    public School completeOnboarding(UUID schoolId) {
        School school = getSchoolById(schoolId);
        if (school.getStatus() == SchoolStatus.ACTIVE) {
            return school;
        }

        OnboardingStatus onboardingStatus = getOnboardingStatus(schoolId);
        if (!onboardingStatus.isAllComplete()) {
            throw new AppException("Cannot complete onboarding. Missing: " + buildMissingSteps(onboardingStatus));
        }

        school.setStatus(SchoolStatus.ACTIVE);
        return schoolRepository.save(school);
    }

    private void validateOnboardingRequest(SchoolOnboardingRequest request) {
        if (!hasText(request.getSchoolCode())) {
            throw new IllegalArgumentException("schoolCode is required");
        }
        if (!hasText(request.getSchoolName())) {
            throw new IllegalArgumentException("schoolName is required");
        }
        if (!hasText(request.getAdminName()) || !hasText(request.getAdminEmail())
                || !hasText(request.getAdminMobile())) {
            throw new IllegalArgumentException("adminName, adminEmail and adminMobile are required");
        }
        if (!hasText(request.getAcademicYearName()) || request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("academicYearName, startDate and endDate are required");
        }
        if (request.getPlanId() == null) {
            throw new IllegalArgumentException("planId is required for onboarding");
        }

        String schoolCode = request.getSchoolCode().trim().toUpperCase();
        if (schoolRepository.existsBySchoolCode(schoolCode)) {
            throw new DuplicateResourceException("School", "schoolCode", schoolCode);
        }
    }

    private boolean isSchoolProfileComplete(School school) {
        return hasText(school.getSchoolCode())
                && hasText(school.getName())
                && hasText(school.getBoard())
                && school.getContactInfo() != null
                && hasText(school.getContactInfo().getContactEmail())
                && hasText(school.getContactInfo().getContactNumber())
                && hasText(school.getContactInfo().getAddress());
    }

    private String buildMissingSteps(OnboardingStatus status) {
        List<String> missing = new ArrayList<>();
        if (!status.isProfileComplete()) {
            missing.add("school profile");
        }
        if (!status.isAcademicYearCreated()) {
            missing.add("academic year");
        }
        if (!status.isAdminUserInvited()) {
            missing.add("admin user");
        }
        if (!status.isSubscriptionActive()) {
            missing.add("subscription");
        }
        return String.join(", ", missing);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String toNullIfBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
