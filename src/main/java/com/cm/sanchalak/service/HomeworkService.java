package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.Homework;
import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.repository.SchoolClassRepository;
import com.cm.sanchalak.repository.HomeworkRepository;
import com.cm.sanchalak.repository.SubjectRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.spec.HomeworkSpecification;
import com.cm.sanchalak.repository.spec.SchoolClassSpecification;
import com.cm.sanchalak.repository.spec.SubjectSpecification;
import com.cm.sanchalak.repository.spec.TeacherSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.security.SchoolContext;
import com.cm.sanchalak.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final OwnershipValidator ownership;

    public Homework createHomework(Long classId, Long subjectId, Long teacherId, String title, String description,
            LocalDate dueDate) {
        SchoolClass clazz = classRepository.findOne(SchoolClassSpecification.activeById(classId))
                .orElseThrow(() -> new RuntimeException("Class not found"));

        Subject subject = subjectRepository.findOne(SubjectSpecification.activeById(subjectId))
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        Teacher teacher = teacherRepository.findOne(TeacherSpecification.activeById(teacherId))
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Homework homework = new Homework();
        homework.setStudentClass(clazz);
        homework.setSubject(subject);
        homework.setTeacher(teacher);
        homework.setTitle(title);
        homework.setDescription(description);
        homework.setDueDate(dueDate);

        return homeworkRepository.save(homework);
    }

    @Transactional(readOnly = true)
    public List<Homework> getAllHomework(Long classId, Long subjectId, LocalDate dueDate) {
        if (classId != null) {
            classRepository.findOne(SchoolClassSpecification.activeById(classId))
                    .orElseThrow(() -> new RuntimeException("Class not found"));
        }

        return homeworkRepository.findAll(HomeworkSpecification.activeScoped()
                .and((root, query, cb) -> {
                    var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                    if (classId != null) {
                        predicates.add(cb.equal(root.get("studentClass").get("id"), classId));
                    }
                    if (subjectId != null) {
                        predicates.add(cb.equal(root.get("subject").get("id"), subjectId));
                    }
                    if (dueDate != null) {
                        predicates.add(cb.equal(root.get("dueDate"), dueDate));
                    }
                    return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                }));
    }

    public Homework updateHomework(Long id, Long classId, Long subjectId, Long teacherId, String title,
            String description, LocalDate dueDate) {
        Homework homework = homeworkRepository.findOne(HomeworkSpecification.activeById(id))
                .orElseThrow(() -> new RuntimeException("Homework not found"));

        if (classId != null) {
            SchoolClass clazz = classRepository.findOne(SchoolClassSpecification.activeById(classId))
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            homework.setStudentClass(clazz);
        }
        if (subjectId != null) {
            Subject subject = subjectRepository.findOne(SubjectSpecification.activeById(subjectId))
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            homework.setSubject(subject);
        }
        if (teacherId != null) {
            Teacher teacher = teacherRepository.findOne(TeacherSpecification.activeById(teacherId))
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            homework.setTeacher(teacher);
        }

        homework.setTitle(title);
        homework.setDescription(description);
        homework.setDueDate(dueDate);

        return homeworkRepository.save(homework);
    }

    public void deleteHomework(Long id) {
        Homework homework = homeworkRepository.findOne(HomeworkSpecification.activeById(id))
                .orElseThrow(() -> new RuntimeException("Homework not found"));

        homeworkRepository.delete(homework);
    }
}
