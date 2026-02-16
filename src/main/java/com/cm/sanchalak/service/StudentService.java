package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.StudentRequest;
import com.cm.sanchalak.dto.StudentResponse;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.repository.SchoolClassRepository;
import com.cm.sanchalak.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    public StudentResponse createStudent(StudentRequest request) {
        SchoolClass studentClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + request.getClassId()));

        Student student = new Student();
        updateStudentFromRequest(student, request);
        student.setStudentClass(studentClass);
        student.setDeleted(false);

        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }

    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));

        if (request.getClassId() != null && !request.getClassId().equals(student.getStudentClass().getId())) {
            SchoolClass studentClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + request.getClassId()));
            student.setStudentClass(studentClass);
        }

        updateStudentFromRequest(student, request);
        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));
        student.setDeleted(true);
        studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> getAllStudents(int page, int size, String sortBy, String sortOrder) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        // Handle case where sortBy might be mapped differently or validate it
        // For now trusting the input matches entity fields
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size, Sort.by(direction, sortBy));

        return studentRepository.findByDeletedFalse(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));
        // Even if deleted, we might want to see it by ID?
        // Typically GET /id should verify existence.
        // If soft deleted, do we 404?
        // Spec usually implies soft-deleted items are gone for general ops.
        // I'll assume 404 if deleted for consistency with the filter.
        if (student.isDeleted()) {
            throw new EntityNotFoundException("Student not found (deleted) with id: " + id);
        }
        return mapToResponse(student);
    }

    // Helper methods
    private void updateStudentFromRequest(Student student, StudentRequest request) {
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setRollNo(request.getRollNo());
        student.setGender(request.getGender());
        student.setGuardianName(request.getGuardianName());
        student.setGuardianMobile(request.getGuardianMobile());
    }

    private StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .studentID("STU-" + student.getId())
                .name(student.getName())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .dateOfBirth("2010-01-01") // Mock for now
                .gender(student.getGender())
                .email("student" + student.getId() + "@school.com") // Mock for now
                .guardianName(student.getGuardianName())
                .guardianMobile(student.getGuardianMobile())
                .classId(student.getStudentClass() != null ? student.getStudentClass().getId() : null)
                .classID(student.getStudentClass() != null ? "CLS-01-" + student.getStudentClass().getId() : "")
                .className(student.getStudentClass() != null ? student.getStudentClass().getName() : "")
                .section("A")
                .rollNo(student.getRollNo())
                .rollNumber(student.getRollNo())
                .admissionNumber(student.getAdmissionNumber())
                .mobileNumber(student.getGuardianMobile())
                .status(student.isDeleted() ? "Inactive" : "Active")
                .deleted(student.isDeleted())
                .address(StudentResponse.AddressResponse.builder()
                        .street("Main Street")
                        .city("City")
                        .state("State")
                        .pincode("123456")
                        .country("Country")
                        .build())
                .primaryParent(StudentResponse.ParentResponse.builder()
                        .name(student.getGuardianName())
                        .relationship("Guardian")
                        .mobileNumber(student.getGuardianMobile())
                        .build())
                .build();
    }
}
