package com.cm.sanchalak;

import com.cm.sanchalak.dto.UpdateAttendanceRequest;
import com.cm.sanchalak.entity.AttendanceRecord;
import com.cm.sanchalak.entity.AttendanceStatus;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.repository.AttendanceRepository;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.SchoolClassRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class AttendanceCorrectionIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SchoolClassRepository classRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    private Long attendanceId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        // Cleaning up DB is risky if other tests are running, but for local it's fine.
        // Actually, @Transactional on test method is better, but this is
        // IntegrationTest class.
        // databaseCleanup.execute(); // skipping for now to rely on dirties context or
        // just unique data?

        // Setup data
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(RoleName.ROLE_STUDENT));
        }

        SchoolClass clazz = new SchoolClass();
        clazz.setName("Class 10-A");
        clazz.setSchoolId(java.util.UUID.randomUUID());
        clazz = classRepository.save(clazz);

        Student student = new Student();
        student.setName("Integration Test Student");
        student.setEmail("correction.student@test.com");
        student.setStudentClass(clazz);
        student.setSchoolId(clazz.getSchoolId());
        // minimal fields...
        student = studentRepository.save(student);

        AttendanceRecord record = new AttendanceRecord();
        record.setStudent(student);
        record.setSchoolClass(clazz);
        record.setDate(LocalDate.now());
        record.setStatus(AttendanceStatus.ABSENT);
        record.setMarkedBy("SYSTEM");
        record = attendanceRepository.save(record);

        attendanceId = record.getId();
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void testUpdateAttendance() throws Exception {
        UpdateAttendanceRequest request = new UpdateAttendanceRequest();
        request.setStatus(AttendanceStatus.PRESENT);
        request.setRemarks("Correction: Student arrived late");

        mockMvc.perform(put("/api/attendance/" + attendanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRESENT"))
                .andExpect(jsonPath("$.remarks").value("Correction: Student arrived late"));
    }
}
