package com.cm.sanchalak.platform.school;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
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
