package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.academic.RoutineRequest;
import com.cm.sanchalak.dto.academic.RoutineResponse;
import com.cm.sanchalak.entity.SchoolClass;
import com.cm.sanchalak.entity.ClassRoutine;
import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.repository.SchoolClassRepository;
import com.cm.sanchalak.repository.ClassRoutineRepository;
import com.cm.sanchalak.repository.SubjectRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RoutineService {

    private final ClassRoutineRepository routineRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;


    public List<RoutineResponse> getRoutineByClassId(Long classId) {
        return routineRepository.findByStudentClassId(classId).stream()
                .map(RoutineResponse::new)
                .collect(Collectors.toList());
    }

    public List<RoutineResponse> getRoutineByTeacherId(Long teacherId) {
        return routineRepository.findByTeacherId(teacherId).stream()
                .map(RoutineResponse::new)
                .collect(Collectors.toList());
    }

    public RoutineResponse assignSlot(RoutineRequest request) {
        // Validate conflicts
        if (routineRepository.existsByStudentClassIdAndDayOfWeekAndPeriod(
                request.getClassId(), request.getDayOfWeek(), request.getPeriod())) {
            throw new IllegalArgumentException("Slot is already booked for this class.");
        }

        if (routineRepository.existsByTeacherIdAndDayOfWeekAndPeriod(
                request.getTeacherId(), request.getDayOfWeek(), request.getPeriod())) {
            throw new IllegalArgumentException("Teacher is already booked in this slot.");
        }

        SchoolClass studentClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        ClassRoutine routine = new ClassRoutine();
        routine.setStudentClass(studentClass);
        routine.setSubject(subject);
        routine.setTeacher(teacher);
        routine.setDayOfWeek(request.getDayOfWeek());
        routine.setPeriod(request.getPeriod());
        routine.setStartTime(request.getStartTime());
        routine.setEndTime(request.getEndTime());

        ClassRoutine savedRoutine = routineRepository.save(routine);
        return new RoutineResponse(savedRoutine);
    }

    public void clearSlot(Long id) {
        if (!routineRepository.existsById(id)) {
            throw new RuntimeException("Routine slot not found");
        }
        routineRepository.deleteById(id);
    }
}
