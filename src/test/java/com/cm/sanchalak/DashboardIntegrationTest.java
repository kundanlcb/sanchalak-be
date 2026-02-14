package com.cm.sanchalak;

import com.cm.sanchalak.dto.LoginRequest;
import com.cm.sanchalak.dto.SignUpRequest;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.SchoolClassRepository;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.annotation.DirtiesContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DashboardIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private WebTestClient webTestClient;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SchoolClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    @BeforeEach
    void setUp() {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(this.context)
                .apply(springSecurity())
                .configureClient()
                .build();
        
        // Use centralized cleanup
        databaseCleanup.cleanAllTables();
        
        createRoleIfMissing(RoleName.ROLE_ADMIN);
        createRoleIfMissing(RoleName.ROLE_TEACHER);
        createRoleIfMissing(RoleName.ROLE_STUDENT);
    }
    
    private void createRoleIfMissing(RoleName roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            roleRepository.save(new Role(roleName));
        }
    }


    @Test
    void shouldReturnDashboardStats() {
        // Seed Data
        SchoolClass cls = new SchoolClass();
        cls.setName("Class 10 A");
        classRepository.save(cls);

        Student student = new Student();
        student.setName("Student 1");
        student.setStudentClass(cls);
        studentRepository.save(student);

        Teacher teacher = new Teacher();
        teacher.setName("Teacher 1");
        teacher.setEmail("t1@files.com");
        teacher.setPhone("1234567890");
        teacherRepository.save(teacher);

        // Create Admin User for Auth
        String adminEmail = "admin_" + System.currentTimeMillis() + "@school.com";
        User admin = new User("Admin", adminEmail, passwordEncoder.encode("password"));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();
        admin.setRoles(Collections.singleton(adminRole));
        String mobile = String.valueOf(System.currentTimeMillis()).substring(3); // Unique
        admin.setMobileNumber(mobile);
        userRepository.save(admin);

        // Login to get token
        LoginRequest loginRequest = new LoginRequest(adminEmail, "password");

        var loginResult = webTestClient.post().uri("/api/auth/signin")
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .returnResult();

        JsonNode loginBody = loginResult.getResponseBody();
        assertThat(loginBody).isNotNull();
        String token;
        if (loginBody.has("data")) {
             assertThat(loginBody.get("data").has("accessToken")).as("accessToken should be present in data").isTrue();
             token = loginBody.get("data").get("accessToken").asText();
        } else {
             assertThat(loginBody.has("accessToken")).as("accessToken should be present").isTrue();
             token = loginBody.get("accessToken").asText();
        }

        // Call Dashboard endpoint
        webTestClient.get().uri("/api/dashboard/stats")
            .headers(headers -> headers.setBearerAuth(token))
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body.get("students").asInt()).isEqualTo(1);
                assertThat(body.get("teachers").asInt()).isEqualTo(1);
                assertThat(body.get("classes").asInt()).isEqualTo(1);
            });
    }
}
