package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.StudentRequest;
import com.cm.sanchalak.dto.StudentResponse;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.SchoolClassRepository;
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
        return mapToResponse(updatedStudent);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findOne(StudentSpecification.activeById(id))
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));

        student.setDeleted(true);
        studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getAllStudents(Long classId, int page, int size, String sortBy, String sortOrder) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size, Sort.by(direction, sortBy));

        org.springframework.data.jpa.domain.Specification<Student> spec = StudentSpecification.activeScoped();
        if (classId != null) {
            spec = spec.and(StudentSpecification.hasByClassId(classId));
        }

        return studentRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
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
            String mobile = request.getMobileNumber() != null ? request.getMobileNumber() : request.getGuardianMobile();
            if (mobile != null)
                student.setGuardianMobile(mobile);
        }
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
                .status(student.isDeleted() ? "Inactive" : "Active")
                .deleted(student.isDeleted())
                .address(StudentResponse.AddressResponse.builder()
                        .street(student.getAddressStreet())
                        .city(student.getAddressCity())
                        .state(student.getAddressState())
                        .pincode(student.getAddressPincode())
                        .country(student.getAddressCountry())
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
