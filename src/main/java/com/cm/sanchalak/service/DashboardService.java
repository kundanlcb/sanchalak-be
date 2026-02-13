package com.cm.sanchalak.service;

import com.cm.sanchalak.repository.ClassRepository;
import com.cm.sanchalak.repository.StudentRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.AttendanceRepository;
import com.cm.sanchalak.entity.AttendanceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

@Service
public class DashboardService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalStudents = studentRepository.countByDeletedFalse();
        
        stats.put("students", totalStudents);
        stats.put("teachers", teacherRepository.countByDeletedFalse());
        stats.put("classes", classRepository.count());
        
        // Attendance Calculation
        if (totalStudents > 0) {
             long present = attendanceRepository.countByDateAndStatus(LocalDate.now(), AttendanceStatus.PRESENT);
             // Simple percentage
             double percent = ((double) present / totalStudents) * 100;
             stats.put("attendance", Math.round(percent)); // Store as Long (e.g., 95)
        } else {
             stats.put("attendance", 0L);
        }
        
        return stats;
    }

    public Map<String, Long> getGenderDistribution() {
        long male = studentRepository.countByGender("MALE");
        long female = studentRepository.countByGender("FEMALE");
        long total = studentRepository.count();
        long other = total - male - female;

        Map<String, Long> dist = new HashMap<>();
        dist.put("MALE", male);
        dist.put("FEMALE", female);
        if (other > 0) {
            dist.put("OTHER", other);
        }
        return dist;
    }

    public List<Map<String, Object>> getTeacherPerformance() {
        // Mock implementation for Phase 1
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> t1 = new HashMap<>();
        t1.put("teacherName", "Mr. Arithmetic");
        t1.put("avgMarks", 78.5);
        list.add(t1);
        
        Map<String, Object> t2 = new HashMap<>();
        t2.put("teacherName", "Ms. Literature");
        t2.put("avgMarks", 82.3);
        list.add(t2);
        
        return list;
    }

    public List<Map<String, Object>> getActivityFeed() {
        List<Map<String, Object>> feed = new ArrayList<>();
        
        Map<String, Object> item1 = new HashMap<>();
        item1.put("id", 1);
        item1.put("message", "New student registered: John Doe");
        item1.put("timestamp", LocalDateTime.now().minusHours(2));
        item1.put("type", "INFO");
        feed.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", 2);
        item2.put("message", "Exam Schedule published for Class X");
        item2.put("timestamp", LocalDateTime.now().minusDays(1));
        item2.put("type", "ALERT");
        feed.add(item2);
        
        return feed;
    }
}
