package com.cm.sanchalak.platform.school;

import com.cm.sanchalak.platform.academic.AcademicYearRepository;
import com.cm.sanchalak.platform.onboarding.OnboardingStatus;
import com.cm.sanchalak.platform.subscription.SchoolSubscriptionRepository;
import com.cm.sanchalak.platform.subscription.SubscriptionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolUserRepository schoolUserRepository;
    private final SchoolSubscriptionRepository subscriptionRepository;

    public SchoolService(SchoolRepository schoolRepository,
            AcademicYearRepository academicYearRepository,
            SchoolUserRepository schoolUserRepository,
            SchoolSubscriptionRepository subscriptionRepository) {
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.schoolUserRepository = schoolUserRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public OnboardingStatus getOnboardingStatus(UUID schoolId) {
        School school = getSchoolById(schoolId);

        boolean profileComplete = school.getStatus() != SchoolStatus.DRAFT;
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

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    public School getSchoolById(UUID schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found: " + schoolId));
    }

    @Transactional
    public School createSchool(School school) {
        if (schoolRepository.existsBySchoolCode(school.getSchoolCode())) {
            throw new RuntimeException("School code already exists: " + school.getSchoolCode());
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

        if (updatedSchool.getName() != null) {
            existingSchool.setName(updatedSchool.getName());
        }
        if (updatedSchool.getBoard() != null) {
            existingSchool.setBoard(updatedSchool.getBoard());
        }
        if (updatedSchool.getContactInfo() != null) {
            existingSchool.setContactInfo(updatedSchool.getContactInfo());
        }
        if (updatedSchool.getRegistrationNumber() != null) {
            existingSchool.setRegistrationNumber(updatedSchool.getRegistrationNumber());
        }
        if (updatedSchool.getTimezone() != null) {
            existingSchool.setTimezone(updatedSchool.getTimezone());
        }

        return schoolRepository.save(existingSchool);
    }
}
