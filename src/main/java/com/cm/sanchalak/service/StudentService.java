package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.StudentRequest;
import com.cm.sanchalak.dto.StudentResponse;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.SchoolClassRepository;
import com.cm.sanchalak.repository.StudentImportStagingRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.platform.master.MasterDataService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cm.sanchalak.exception.AppException;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.cm.sanchalak.repository.spec.StudentSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.security.SchoolContext;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;
    private final MasterDataService masterDataService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OwnershipValidator ownership;
    private final StudentImportStagingRepository stagingRepository;

    public StudentResponse createStudent(StudentRequest request) {
        masterDataService.validateValue("GENDER", request.getGender());

        SchoolClass studentClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + request.getClassId()));

        ownership.validate(studentClass.getSchoolId());

        UUID schoolId = SchoolContext.getSchoolId();

        Student student = new Student();
        updateStudentFromRequest(student, request);
        student.setSchoolId(schoolId);
        student.setStudentClass(studentClass);
        student.setDeleted(false);
        student.setEmail(request.getEmail());

        // Create User account
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already in use: " + request.getEmail());
        }

        User user = new User();
        user.setName(student.getName());
        user.setEmail(student.getEmail());
        user.setMobileNumber(student.getGuardianMobile()); // Student mobile or parent fallback
        user.setPassword(passwordEncoder.encode(request.getEmail())); // Default password is email

        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new EntityNotFoundException("Student Role not found"));
        user.setRoles(Collections.singleton(studentRole));
        user.setSchoolId(schoolId);

        User savedUser = userRepository.save(user);
        student.setUserId(savedUser.getId());

        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }

    public StudentResponse updateStudent(Long id, StudentRequest request) {
        masterDataService.validateValue("GENDER", request.getGender());

        Student student = studentRepository.findOne(StudentSpecification.activeById(id))
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));

        if (request.getClassId() != null && !request.getClassId().equals(student.getStudentClass().getId())) {
            SchoolClass studentClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + request.getClassId()));

            // Validate that the new class also belongs to the same school
            ownership.validate(studentClass.getSchoolId());

            student.setStudentClass(studentClass);
        }

        updateStudentFromRequest(student, request);
        Student updatedStudent = studentRepository.save(student);

        // Sync mobile number with User account if it exists
        if (updatedStudent.getUserId() != null) {
            userRepository.findById(updatedStudent.getUserId()).ifPresent(user -> {
                user.setMobileNumber(updatedStudent.getGuardianMobile());
                userRepository.save(user);
            });
        }

        return mapToResponse(updatedStudent);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findOne(StudentSpecification.activeById(id))
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));

        student.setDeleted(true);
        studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getAllStudents(Long classId, StudentStatus status, String search, int page, int size,
            String sortBy, String sortOrder) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size, Sort.by(direction, sortBy));

        org.springframework.data.jpa.domain.Specification<Student> spec = StudentSpecification.activeScoped();
        if (classId != null) {
            spec = spec.and(StudentSpecification.hasByClassId(classId));
        }
        if (status != null) {
            spec = spec.and(StudentSpecification.hasStatus(status));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(StudentSpecification.search(search));
        }

        return studentRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<StudentImportStaging> getImportDrafts() {
        return stagingRepository.findAll(); // Simple catch-all for now
    }

    @Transactional
    public StudentResponse onboardFromStaging(Long stagingId) {
        StudentImportStaging staging = stagingRepository.findById(stagingId)
                .orElseThrow(() -> new EntityNotFoundException("Staging record not found: " + stagingId));

        try {
            // Map staging to request for reuse of existing create logic
            StudentRequest request = new StudentRequest();
            request.setName(
                    staging.getFirstName() + (staging.getLastName() != null ? " " + staging.getLastName() : ""));
            request.setFirstName(staging.getFirstName());
            request.setLastName(staging.getLastName());
            request.setEmail(staging.getEmail());
            request.setMobileNumber(staging.getPhone());
            request.setAdmissionNumber(staging.getAdmissionNo());
            request.setGuardianName(staging.getParentName());
            request.setGuardianMobile(staging.getParentPhone());

            // Default required fields for manual completion later if missing
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new AppException("Email is required for onboarding");
            }

            // Find class by name if provided
            if (staging.getClassName() != null) {
                classRepository.findOne((root, query, cb) -> cb.equal(root.get("name"), staging.getClassName()))
                        .ifPresent(sc -> request.setClassId(sc.getId()));
            }

            if (request.getClassId() == null) {
                // Fallback to a default class or throw error if mandatory
                // For now, we'll let it fail validation in createStudent if mandatory
            }

            StudentResponse response = createStudent(request);

            // Mark staging as processed and clear error
            staging.setProcessed(true);
            staging.setErrorMessage(null);
            stagingRepository.save(staging);

            return response;
        } catch (Exception e) {
            staging.setErrorMessage(e.getMessage());
            stagingRepository.save(staging);
            throw e;
        }
    }

    public StudentResponse approveStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));

        student.setStatus(StudentStatus.ACTIVE);
        return mapToResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findOne(StudentSpecification.activeById(id))
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));

        return mapToResponse(student);
    }

    private void updateStudentFromRequest(Student student, StudentRequest request) {
        // Handle name splitting if name is provided but first/last are missing
        if (request.getName() != null && !request.getName().isBlank()) {
            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                String[] parts = request.getName().trim().split("\\s+");
                student.setFirstName(parts[0]);
                if (parts.length > 1) {
                    student.setLastName(parts[parts.length - 1]);
                }
            } else {
                student.setFirstName(request.getFirstName());
                student.setLastName(request.getLastName());
            }
        } else {
            student.setFirstName(request.getFirstName());
            student.setLastName(request.getLastName());
        }

        // Handle dual-field rollNumber/rollNo
        Integer rollNumber = request.getRollNumber() != null ? request.getRollNumber() : request.getRollNo();
        student.setRollNo(rollNumber);

        student.setGender(request.getGender());

        // Admission Details
        student.setAdmissionNumber(request.getAdmissionNumber());
        if (request.getAdmissionDate() != null) {
            student.setAdmissionDate(java.time.LocalDate.parse(request.getAdmissionDate()));
        }
        student.setSection(request.getSection());
        student.setAcademicYear(request.getAcademicYear());
        if (request.getDateOfBirth() != null) {
            student.setDateOfBirth(java.time.LocalDate.parse(request.getDateOfBirth()));
        }
        student.setBloodGroup(request.getBloodGroup());

        // Address Mapping
        if (request.getAddress() != null) {
            student.setAddressStreet(request.getAddress().getStreet());
            student.setAddressCity(request.getAddress().getCity());
            student.setAddressState(request.getAddress().getState());
            student.setAddressPincode(request.getAddress().getPincode());
            student.setAddressCountry(request.getAddress().getCountry());
            if (request.getAddress().getVillage() != null)
                student.setAddressVillage(request.getAddress().getVillage());
            if (request.getAddress().getDistrict() != null)
                student.setAddressDistrict(request.getAddress().getDistrict());
        }

        // Parent Mapping
        if (request.getPrimaryParent() != null) {
            student.setGuardianName(request.getPrimaryParent().getName());
            student.setGuardianMobile(request.getPrimaryParent().getMobileNumber());
            student.setParentRelationship(request.getPrimaryParent().getRelationship());
            student.setParentEmail(request.getPrimaryParent().getEmail());
            student.setParentOccupation(request.getPrimaryParent().getOccupation());
        } else {
            // Fallback for legacy fields
            if (request.getGuardianName() != null)
                student.setGuardianName(request.getGuardianName());
            String mobile = request.getGuardianMobile();
            if (mobile != null)
                student.setGuardianMobile(mobile);
        }

        // Extended identity fields — null-guarded so existing data is never overwritten
        if (request.getFatherName() != null)
            student.setFatherName(request.getFatherName());
        if (request.getMotherName() != null)
            student.setMotherName(request.getMotherName());
        if (request.getStudentAadhar() != null)
            student.setStudentAadhar(request.getStudentAadhar());
        if (request.getFatherAadhar() != null)
            student.setFatherAadhar(request.getFatherAadhar());
        if (request.getMotherAadhar() != null)
            student.setMotherAadhar(request.getMotherAadhar());
        if (request.getNationality() != null)
            student.setNationality(request.getNationality());
        if (request.getIsDisabled() != null)
            student.setIsDisabled(request.getIsDisabled());
        if (request.getPhotoUrl() != null)
            student.setPhotoUrl(request.getPhotoUrl());

        // Student Phone Fallback: If student mobile is missing, use parent mobility
        String studentMobile = request.getMobileNumber();
        if ((studentMobile == null || studentMobile.isBlank()) && student.getGuardianMobile() != null) {
            studentMobile = student.getGuardianMobile();
        }
        // Although currently stored in guardian_mobile in Student entity for some
        // reason,
        // we should ensure it flows to the User entity correctly in createStudent.
        // If we have a mobileNumber field in Student, use it. But current Student.java
        // seems to rely on guardianMobile for the primary contact number.
    }

    private StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .userId(student.getUserId() != null ? student.getUserId().toString() : null)
                .createdAt(student.getCreatedAt() != null ? student.getCreatedAt().toString() : null)
                .updatedAt(student.getUpdatedAt() != null ? student.getUpdatedAt().toString() : null)
                .name(student.getName())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .dateOfBirth(student.getDateOfBirth() != null ? student.getDateOfBirth().toString() : null)
                .gender(student.getGender())
                .email(student.getEmail())
                .guardianName(student.getGuardianName())
                .guardianMobile(student.getGuardianMobile())
                .classId(student.getStudentClass() != null ? student.getStudentClass().getId() : null)
                .classID(student.getStudentClass() != null ? String.valueOf(student.getStudentClass().getId()) : "")
                .className(student.getStudentClass() != null ? student.getStudentClass().getName() : "")
                .studentClass(student.getStudentClass() != null ? StudentResponse.ClassResponse.builder()
                        .id(student.getStudentClass().getId())
                        .name(student.getStudentClass().getName())
                        .build() : null)
                .section(student.getSection())
                .rollNo(student.getRollNo())
                .rollNumber(student.getRollNo())
                .admissionNumber(student.getAdmissionNumber())
                .admissionDate(student.getAdmissionDate() != null ? student.getAdmissionDate().toString() : null)
                .mobileNumber(student.getGuardianMobile())
                .status(student.getStatus() != null ? student.getStatus().name() : "Active")
                .deleted(student.isDeleted())
                .bloodGroup(student.getBloodGroup())
                // Extended identity fields
                .fatherName(student.getFatherName())
                .motherName(student.getMotherName())
                .studentAadhar(student.getStudentAadhar())
                .fatherAadhar(student.getFatherAadhar())
                .motherAadhar(student.getMotherAadhar())
                .nationality(student.getNationality())
                .isDisabled(student.getIsDisabled())
                .photoUrl(student.getPhotoUrl())
                .address(StudentResponse.AddressResponse.builder()
                        .street(student.getAddressStreet())
                        .city(student.getAddressCity())
                        .state(student.getAddressState())
                        .pincode(student.getAddressPincode())
                        .country(student.getAddressCountry())
                        .village(student.getAddressVillage())
                        .district(student.getAddressDistrict())
                        .build())
                .primaryParent(StudentResponse.ParentResponse.builder()
                        .name(student.getGuardianName())
                        .relationship(student.getParentRelationship())
                        .mobileNumber(student.getGuardianMobile())
                        .email(student.getParentEmail())
                        .occupation(student.getParentOccupation())
                        .build())
                .build();
    }
}
