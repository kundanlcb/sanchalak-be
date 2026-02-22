package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.StudentRequest;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.platform.master.MasterDataService;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.SchoolClassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceMasterDataTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SchoolClassRepository classRepository;

    @Mock
    private MasterDataService masterDataService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.cm.sanchalak.security.OwnershipValidator ownership;

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
                .email("john@example.com")
                .build();

        schoolClass = new SchoolClass();
        schoolClass.setId(1L);
        schoolClass.setName("Class 1");
    }

    @Test
    void createStudent_ShouldValidateGender() {
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_STUDENT)).thenReturn(Optional.of(new Role(RoleName.ROLE_STUDENT)));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
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
