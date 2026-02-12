package com.cm.sanchalak;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldReturnDashboardStats() throws Exception {
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
        String jsonRequest = "{\"email\":\"admin@school.com\",\"password\":\"password\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        String token = new ObjectMapper().readTree(response).get("accessToken").asText();

        // Call Dashboard endpoint
        mockMvc.perform(get("/api/dashboard/stats")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students", is(1)))
                .andExpect(jsonPath("$.teachers", is(1)))
                .andExpect(jsonPath("$.classes", is(1)));
    }
}
