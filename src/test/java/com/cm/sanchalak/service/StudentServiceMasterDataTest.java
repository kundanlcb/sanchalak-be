package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.StudentRequest;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.platform.master.MasterDataService;
import com.cm.sanchalak.repository.SchoolClassRepository;
import com.cm.sanchalak.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceMasterDataTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SchoolClassRepository classRepository;

    @Mock
    private MasterDataService masterDataService;

    @InjectMocks
    private StudentService studentService;

    private StudentRequest request;
    private SchoolClass schoolClass;

    @BeforeEach
    void setUp() {
        request = StudentRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .rollNo(101)
                .gender("MALE")
                .classId(1L)
                .build();

        schoolClass = new SchoolClass();
        schoolClass.setId(1L);
        schoolClass.setName("Class 1");
    }

    @Test
    void createStudent_ShouldValidateGender() {
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        studentService.createStudent(request);

        verify(masterDataService).validateValue("GENDER", "MALE");
    }

    @Test
    void createStudent_ShouldThrowException_WhenValidationFails() {
        doThrow(new RuntimeException("Invalid value")).when(masterDataService).validateValue("GENDER", "INVALID");
        request.setGender("INVALID");

        try {
            studentService.createStudent(request);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(masterDataService).validateValue("GENDER", "INVALID");
        verify(studentRepository, never()).save(any(Student.class));
    }
}
