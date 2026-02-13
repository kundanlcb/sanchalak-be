package com.cm.sanchalak;

import com.cm.sanchalak.dto.academic.*;
import com.cm.sanchalak.dto.*;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.entity.SchoolClass;
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
import org.springframework.http.MediaType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.annotation.DirtiesContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SchoolOperationsIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private TeacherRepository teacherRepo;
    @Autowired private SubjectRepository subjectRepo;
    @Autowired private SchoolClassRepository classRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private ClassRoutineRepository routineRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private UserRepository userRepo;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
        routineRepo.deleteAll();
        studentRepo.deleteAll();
        teacherRepo.deleteAll();
        userRepo.deleteAll();
        // roleRepo.deleteAll(); // Do not delete roles to avoid constraint violations

        // Setup Roles
        createRoleIfMissing(RoleName.ROLE_ADMIN);
        createRoleIfMissing(RoleName.ROLE_TEACHER);
        createRoleIfMissing(RoleName.ROLE_STUDENT);

        // Clear related academic data
        classRepo.deleteAll(); // Subjects usually depend on nothing but Classes depend on nothing. 
        // Order: Teachers -> Users. Students -> Class. Routines -> Class/Teacher/Subject.
        // Routine deleted first.
        subjectRepo.deleteAll(); // Teachers -> Specialization -> Subject. Teacher deleted. Ok.
        // Actually Teacher -> Specialization is ManyToMany. Teacher deleted -> Join table cleared. Subject safe to delete.
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testTeacherCreationAndSubjectLinking() {
        // 1. Create Subject
        Subject sub = new Subject();
        sub.setName("Physics");
        sub.setCode("PHY101");
        sub = subjectRepo.save(sub);

        // 2. Create Teacher Request
        TeacherRequest req = new TeacherRequest();
        req.setName("Dr. Physics");
        req.setEmail("phy@school.com");
        req.setPhone("1234567890");
        req.setSpecializationIds(Collections.singletonList(sub.getId()));

        // 3. POST /api/academics/teachers
        webTestClient.post().uri("/api/academics/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherResponse.class)
                .consumeWith(res -> {
                    TeacherResponse t = res.getResponseBody();
                    assertNotNull(t);
                    assertEquals("Dr. Physics", t.getName());
                    assertEquals(1, t.getSpecializations().size());
                    assertEquals("Physics", t.getSpecializations().iterator().next().getName());
                });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRoutineConflictValidation() {
        // Setup
        SchoolClass clsEntity = new SchoolClass();
        clsEntity.setName("10-A");
        clsEntity = classRepo.save(clsEntity);

        Subject sub = subjectRepo.save(new Subject("Math", "M1"));
        
        Teacher t = new Teacher();
        t.setName("Mr. Math");
        t.setEmail("math@test.com");
        t.setPhone("1234567890"); // Fix: Add phone
        t = teacherRepo.save(t);

        // Assign Slot 1
        RoutineRequest req1 = new RoutineRequest();
        req1.setClassId(clsEntity.getId());
        req1.setSubjectId(sub.getId());
        req1.setTeacherId(t.getId());
        req1.setDayOfWeek(DayOfWeek.MONDAY);
        req1.setPeriod(1);
        req1.setStartTime(LocalTime.of(10, 0));
        req1.setEndTime(LocalTime.of(11, 0));

        webTestClient.post().uri("/api/academics/routine")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req1)
                .exchange()
                .expectStatus().isOk();

        // Conflict: Same Teacher, Same Time (Period/Day)
        RoutineRequest req2 = new RoutineRequest();
        SchoolClass cls2 = new SchoolClass();
        cls2.setName("10-B");
        cls2 = classRepo.save(cls2);
        req2.setClassId(cls2.getId());
        req2.setSubjectId(sub.getId());
        req2.setTeacherId(t.getId());
        req2.setDayOfWeek(DayOfWeek.MONDAY);
        req2.setPeriod(1); // Same period
        req2.setStartTime(LocalTime.of(10, 0));
        req2.setEndTime(LocalTime.of(11, 0));

        webTestClient.post().uri("/api/academics/routine")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req2)
                .exchange()
                .expectStatus().isBadRequest(); // Conflict expected
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSafeDeleteClass() {
        // 1. Create Class
        SchoolClass cls = new SchoolClass();
        cls.setName("9-C");
        cls = classRepo.save(cls);

        // 2. Add Student
        Student s = new Student();
        s.setName("Student 1");
        s.setStudentClass(cls);
        studentRepo.save(s);

        // 3. Try Delete Class -> Fail (Expecting 400 or 409)
        webTestClient.delete().uri("/api/academic/classes/" + cls.getId())
                .exchange()
                .expectStatus().isBadRequest();

        // 4. Delete Student
        studentRepo.deleteAll();

        // 5. Delete Class -> Success
        webTestClient.delete().uri("/api/academic/classes/" + cls.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    private void createRoleIfMissing(RoleName roleName) {
        if (roleRepo.findByName(roleName).isEmpty()) {
             try {
                 roleRepo.save(new Role(roleName));
             } catch (Exception e) {
                 // Ignore if added concurrently or exists
             }
        }
    }
}
