package com.cm.sanchalak;

import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.repository.SchoolClassRepository;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import java.util.UUID;
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
        private RoleRepository roleRepository;

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
        @WithMockUser(roles = { "SCHOOL_ADMIN" })
        void shouldReturnDashboardStats() {
                // Seed Data
                SchoolClass cls = classRepository.save(SchoolClass.builder()
                                .name("Class 10 A")
                                .classID("C10A")
                                .schoolId(UUID.randomUUID())
                                .build());

                studentRepository.save(Student.builder()
                                .name("Student 1")
                                .email("student1@school.com")
                                .studentClass(cls)
                                .build());

                teacherRepository.save(Teacher.builder()
                                .name("Teacher 1")
                                .email("t1@school.com")
                                .mobileNumber("1234567890")
                                .teacherID("T101")
                                .build());

                // Call Dashboard endpoint
                var statsResult = webTestClient.get().uri("/api/dashboard/stats")
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(String.class)
                                .returnResult();

                String statsBody = statsResult.getResponseBody();
                System.out.println("Stats Response: " + statsBody);
                assertThat(statsBody).isNotNull();

                assertThat(statsBody).contains("\"students\":1");
                assertThat(statsBody).contains("\"teachers\":1");
                assertThat(statsBody).contains("\"classes\":1");
        }
}
