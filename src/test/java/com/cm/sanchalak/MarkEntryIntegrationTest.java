package com.cm.sanchalak;

import com.cm.sanchalak.dto.academic.MarkEntryRequest;
import com.cm.sanchalak.entity.AttendanceStatus;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.ExamSchedule;
import com.cm.sanchalak.entity.ExamTerm;
import com.cm.sanchalak.entity.Student;
import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import org.springframework.test.annotation.DirtiesContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MarkEntryIntegrationTest {

        @Autowired
        private WebApplicationContext context;

        @Autowired
        private ExamTermRepository termRepo;
        @Autowired
        private SchoolClassRepository classRepo;
        @Autowired
        private SubjectRepository subjectRepo;
        @Autowired
        private StudentRepository studentRepo;
        @Autowired
        private ExamScheduleRepository scheduleRepo;
        @Autowired
        private StudentMarksRepository marksRepo;
        @Autowired
        private ClassSubjectRepository classSubjectRepo;
        @Autowired
        private AttendanceRepository attendanceRepo;
        @Autowired
        private TeacherRepository teacherRepo; // Add TeacherRepo

        private WebTestClient webTestClient;

        @BeforeEach
        void setUp() {
                webTestClient = MockMvcWebTestClient.bindToApplicationContext(context)
                                .apply(springSecurity())
                                .configureClient()
                                .build();
                attendanceRepo.deleteAll();
                marksRepo.deleteAll();
                scheduleRepo.deleteAll();

                // Clean dependent entities
                teacherRepo.deleteAll();

                classSubjectRepo.deleteAll();
                subjectRepo.deleteAll();
                termRepo.deleteAll();
                studentRepo.deleteAll();
                classRepo.deleteAll();
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        void testMarkEntryValidation() {
                // Setup Entities
                java.util.UUID schoolId = java.util.UUID.randomUUID();

                com.cm.sanchalak.security.UserPrincipal principal = new com.cm.sanchalak.security.UserPrincipal(
                                java.util.UUID.randomUUID(), "Admin", "admin", "admin@test.com", "password",
                                schoolId, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

                ExamTerm term = new ExamTerm();
                term.setName("Test Term");
                term.setStartDate(LocalDate.now());
                term.setEndDate(LocalDate.now().plusDays(10));
                term.setSchoolId(schoolId);
                term = termRepo.save(term);

                SchoolClass clazz = new SchoolClass();
                clazz.setName("Test Class");
                clazz.setSchoolId(schoolId);
                clazz = classRepo.save(clazz);

                Subject subject = new Subject();
                subject.setName("Math");
                subject.setCode("MATH101");
                subject.setSchoolId(schoolId);
                subject = subjectRepo.save(subject);

                Student student = new Student();
                student.setName("John Doe");
                student.setEmail("john.doe@test.com");
                student.setSchoolId(schoolId);
                student = studentRepo.save(student);

                ExamSchedule schedule = new ExamSchedule();
                schedule.setExamTerm(term);
                schedule.setStudentClass(clazz);
                schedule.setSubject(subject);
                schedule.setExamDate(LocalDate.now());
                schedule.setMaxMarks(100);
                schedule.setSchoolId(schoolId);
                schedule = scheduleRepo.save(schedule);

                // 1. Valid Mark
                MarkEntryRequest req1 = new MarkEntryRequest();
                req1.setExamScheduleId(schedule.getId());
                req1.setStudentId(student.getId());
                req1.setMarksObtained(90.0);
                req1.setRemarks("Good");

                webTestClient.post().uri("/api/academic/marks")
                                .bodyValue(req1)
                                .exchange()
                                .expectStatus().isOk();

                // 2. Invalid Mark (Obtained > Max)
                MarkEntryRequest req2 = new MarkEntryRequest();
                req2.setExamScheduleId(schedule.getId());
                req2.setStudentId(student.getId());
                req2.setMarksObtained(150.0);

                webTestClient.post().uri("/api/academic/marks")
                                .bodyValue(req2)
                                .exchange()
                                .expectStatus().isBadRequest();
        }
}
