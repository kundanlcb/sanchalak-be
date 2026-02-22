package com.cm.sanchalak;

import com.cm.sanchalak.dto.BulkMarkAttendanceRequest;
import com.cm.sanchalak.dto.LoginRequest;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.entity.SchoolClass;
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
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AttendanceIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private WebTestClient webTestClient;

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private SchoolClassRepository classRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DatabaseCleanup databaseCleanup;

    private ObjectMapper objectMapper = new ObjectMapper();

    private String teacherEmail = "teacher_" + System.currentTimeMillis() + "@test.com"; // Unique email
    private String token;
    private Long classId;
    private Long studentId;

    @BeforeEach
    void setUp() throws Exception {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(this.context)
                .apply(springSecurity())
                .configureClient()
                .build();

        // Use database cleanup utility to handle FK constraints
        databaseCleanup.cleanAllTables();

        // 1. Roles
        createRoleIfMissing(RoleName.ROLE_TEACHER);
        createRoleIfMissing(RoleName.ROLE_STUDENT);
        createRoleIfMissing(RoleName.ROLE_ADMIN);

        java.util.UUID testSchoolId = java.util.UUID.randomUUID();

        // 2. Teacher
        User teacher = new User();
        teacher.setName("Test Teacher");
        teacher.setEmail(teacherEmail);
        teacher.setPassword(passwordEncoder.encode("password"));
        teacher.setSchoolId(testSchoolId);
        Role teacherRole = roleRepository.findByName(RoleName.ROLE_TEACHER).orElseThrow();
        teacher.setRoles(Set.of(teacherRole));
        String mobile = String.valueOf(System.currentTimeMillis()).substring(3);
        teacher.setMobileNumber(mobile);
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
            String respStr = new String(response);
            System.out.println("Login Response: " + respStr);
            JsonNode root = objectMapper.readTree(response);
            if (root.has("data")) {
                this.token = root.path("data").path("accessToken").asText();
            } else {
                this.token = root.path("accessToken").asText();
            }
        }
        assertThat(token).as("JWT Token").isNotEmpty();

        // 4. Class & Student
        SchoolClass clazz = new SchoolClass();
        clazz.setName("Class 1A");
        clazz.setSchoolId(testSchoolId);
        clazz = classRepository.save(clazz);
        this.classId = clazz.getId();

        Student student = new Student();
        student.setName("Student 1");
        student.setEmail("attendance.student@test.com");
        student.setSchoolId(testSchoolId);
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

    private void createRoleIfMissing(RoleName roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            roleRepository.save(new Role(roleName));
        }
    }
}
