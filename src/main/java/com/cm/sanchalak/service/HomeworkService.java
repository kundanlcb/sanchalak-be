package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.Homework;
import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.repository.ClassRepository;
import com.cm.sanchalak.repository.HomeworkRepository;
import com.cm.sanchalak.repository.SubjectRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    public HomeworkService(HomeworkRepository homeworkRepository, ClassRepository classRepository,
                           SubjectRepository subjectRepository, TeacherRepository teacherRepository) {
        this.homeworkRepository = homeworkRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
    }

    public Homework createHomework(Long classId, Long subjectId, Long teacherId, String title, String description, LocalDate dueDate) {
        com.cm.sanchalak.entity.Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        Teacher teacher = teacherRepository.findById(teacherId)
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

    public List<Homework> getAllHomework() {
        return homeworkRepository.findAll();
    }
}
