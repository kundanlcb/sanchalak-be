package com.cm.sanchalak;

import com.cm.sanchalak.dto.BulkMarkAttendanceRequest;
import com.cm.sanchalak.dto.LoginRequest;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.entity.Class;
import com.cm.sanchalak.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
public class AttendanceIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private WebTestClient webTestClient;

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    private String teacherEmail = "teacher@test.com";
    private String token;
    private Long classId;
    private Long studentId;

    @BeforeEach
    void setUp() throws Exception {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(this.context)
                .apply(springSecurity())
                .configureClient()
                .build();
        
        attendanceRepository.deleteAll();
        studentRepository.deleteAll();
        classRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Roles
        if (roleRepository.count() == 0) {
            for (RoleName roleName : RoleName.values()) {
                roleRepository.save(new Role(roleName));
            }
        }

        // 2. Teacher
        User teacher = new User();
        teacher.setName("Test Teacher");
        teacher.setEmail(teacherEmail);
        teacher.setPassword(passwordEncoder.encode("password"));
        Role teacherRole = roleRepository.findByName(RoleName.ROLE_TEACHER).orElseThrow();
        teacher.setRoles(java.util.Set.of(teacherRole));
        userRepository.save(teacher);

        // 3. Login
        LoginRequest loginRequest = new LoginRequest(teacherEmail, "password");
        byte[] response = webTestClient.post().uri("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .returnResult(byte[].class)
                .getResponseBody()
                .blockFirst();
        
        if (response != null) {
            JsonNode root = objectMapper.readTree(response);
            this.token = root.path("accessToken").asText();
        }
        assertThat(token).as("JWT Token").isNotNull();

        // 4. Class & Student
        Class clazz = new Class();
        clazz.setName("Class 1A");
        clazz = classRepository.save(clazz);
        this.classId = clazz.getId();

        Student student = new Student();
        student.setName("Student 1");
        student.setStudentClass(clazz); 
        student = studentRepository.save(student);
        this.studentId = student.getId();
    }

    @Test
    void markBulkAttendance_ShouldSucceed() {
        BulkMarkAttendanceRequest request = new BulkMarkAttendanceRequest();
        request.setClassId(classId);
        request.setDate(LocalDate.now());
        
        BulkMarkAttendanceRequest.StudentAttendanceStatus status = new BulkMarkAttendanceRequest.StudentAttendanceStatus();
        status.setStudentId(studentId);
        status.setStatus(AttendanceStatus.PRESENT);
        
        request.setAttendances(List.of(status));

        webTestClient.post().uri("/api/attendance/bulk")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectBody()
                .consumeWith(r -> {
                    System.out.println("STATUS: " + r.getStatus());
                    if (r.getResponseBody() != null) {
                        System.out.println("BODY: " + new String(r.getResponseBody()));
                    }
                })
                .jsonPath("$.markedCount").isEqualTo(1);

        assertThat(attendanceRepository.findByStudentIdAndDate(studentId, LocalDate.now())).isPresent();
    }
}
