package com.cm.sanchalak;

import com.cm.sanchalak.dto.MarkEntryRequest;
import com.cm.sanchalak.entity.*;
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

@SpringBootTest
@ActiveProfiles("test")
public class MarkEntryIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired private ExamTermRepository termRepo;
    @Autowired private ClassRepository classRepo;
    @Autowired private SubjectRepository subjectRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private ExamScheduleRepository scheduleRepo;
    @Autowired private StudentMarksRepository marksRepo;
    @Autowired private ClassSubjectRepository classSubjectRepo;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(context).build();
        marksRepo.deleteAll();
        scheduleRepo.deleteAll();
        classSubjectRepo.deleteAll();
        subjectRepo.deleteAll();
        termRepo.deleteAll();
        studentRepo.deleteAll();
        classRepo.deleteAll();
    }

    @Test
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void testMarkEntryValidation() {
        // Setup Entities
        ExamTerm term = new ExamTerm();
        term.setName("Test Term");
        term.setStartDate(LocalDate.now());
        term.setEndDate(LocalDate.now().plusDays(10));
        term = termRepo.save(term);

        com.cm.sanchalak.entity.Class clazz = new com.cm.sanchalak.entity.Class();
        clazz.setName("Test Class");
        clazz = classRepo.save(clazz);

        Subject subject = new Subject();
        subject.setName("Math");
        subject.setCode("MATH101");
        subject = subjectRepo.save(subject);

        Student student = new Student();
        student.setName("John Doe");
        student = studentRepo.save(student);

        ExamSchedule schedule = new ExamSchedule();
        schedule.setExamTerm(term);
        schedule.setStudentClass(clazz);
        schedule.setSubject(subject);
        schedule.setExamDate(LocalDate.now());
        schedule.setMaxMarks(100);
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
