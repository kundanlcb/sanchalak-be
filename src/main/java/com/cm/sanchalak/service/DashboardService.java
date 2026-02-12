package com.cm.sanchalak.service;

import com.cm.sanchalak.repository.ClassRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ClassRepository classRepository;

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("students", studentRepository.count());
        stats.put("teachers", teacherRepository.count());
        stats.put("classes", classRepository.count());
        return stats;
    }
}
