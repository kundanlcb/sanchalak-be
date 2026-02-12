package com.cm.sanchalak;

import com.cm.sanchalak.dto.LoginRequest;
import com.cm.sanchalak.dto.SignUpRequest;
import com.cm.sanchalak.entity.Class;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.ClassRepository;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
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

@SpringBootTest
@ActiveProfiles("test")
public class DashboardIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private WebTestClient webTestClient;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(this.context).build();
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
        classRepository.deleteAll();
        userRepository.deleteAll();
        
        if (roleRepository.count() == 0) {
            for (RoleName roleName : RoleName.values()) {
                roleRepository.save(new Role(roleName));
            }
        }
    }


    @Test
    void shouldReturnDashboardStats() {
        // Seed Data
        Class cls = new Class();
        cls.setName("Class 10 A");
        classRepository.save(cls);

        Student student = new Student();
        student.setName("Student 1");
        student.setStudentClass(cls);
        studentRepository.save(student);

        Teacher teacher = new Teacher();
        teacher.setName("Teacher 1");
        teacherRepository.save(teacher);

        // Create Admin User for Auth
        User admin = new User("Admin", "admin@school.com", passwordEncoder.encode("password"));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();
        admin.setRoles(Collections.singleton(adminRole));
        userRepository.save(admin);

        // Login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@school.com");
        loginRequest.setPassword("password");

        var loginResult = webTestClient.post().uri("/api/auth/signin")
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .returnResult();

        JsonNode loginBody = loginResult.getResponseBody();
        assertThat(loginBody).isNotNull();
        assertThat(loginBody.has("accessToken")).as("accessToken should be present").isTrue();

        String token = loginBody.get("accessToken").asText();

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
