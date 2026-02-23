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
import com.cm.sanchalak.repository.spec.RoutineSpecification;
import com.cm.sanchalak.repository.spec.SchoolClassSpecification;
import com.cm.sanchalak.repository.spec.SubjectSpecification;
import com.cm.sanchalak.repository.spec.TeacherSpecification;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.entity.TimetableSlot;
import com.cm.sanchalak.repository.TimetableSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoutineService {

        private final ClassRoutineRepository routineRepository;
        private final SchoolClassRepository classRepository;
        private final SubjectRepository subjectRepository;
        private final TeacherRepository teacherRepository;

        private final TimetableSlotRepository timetableSlotRepository;

        @Transactional(readOnly = true)
        public List<RoutineResponse> getRoutineByClassId(Long classId) {
                SchoolClass studentClass = classRepository.findOne(SchoolClassSpecification.activeById(classId))
                                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

                List<TimetableSlot> slots = timetableSlotRepository
                                .findBySchoolIdOrderByOrderIndexAsc(studentClass.getSchoolId());
                Map<Integer, TimetableSlot> slotMap = slots.stream()
                                .collect(Collectors.toMap(TimetableSlot::getOrderIndex, s -> s));

                return routineRepository.findAll(RoutineSpecification.activeScoped()
                                .and((root, query, cb) -> cb.equal(root.get("studentClass").get("id"), classId)))
                                .stream()
                                .map(routine -> mergeSlotTimes(new RoutineResponse(routine), slotMap))
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<RoutineResponse> getRoutineByTeacherId(Long teacherId) {
                Teacher teacher = teacherRepository.findOne(TeacherSpecification.activeById(teacherId))
                                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

                List<TimetableSlot> slots = timetableSlotRepository
                                .findBySchoolIdOrderByOrderIndexAsc(teacher.getSchoolId());
                Map<Integer, TimetableSlot> slotMap = slots.stream()
                                .collect(Collectors.toMap(TimetableSlot::getOrderIndex, s -> s));

                return routineRepository.findAll(RoutineSpecification.activeScoped()
                                .and((root, query, cb) -> cb.equal(root.get("teacher").get("id"), teacherId)))
                                .stream()
                                .map(routine -> mergeSlotTimes(new RoutineResponse(routine), slotMap))
                                .collect(Collectors.toList());
        }

        private RoutineResponse mergeSlotTimes(RoutineResponse response, Map<Integer, TimetableSlot> slotMap) {
                TimetableSlot mappedSlot = slotMap.get(response.getPeriod());
                if (mappedSlot != null) {
                        response.setStartTime(mappedSlot.getStartTime());
                        response.setEndTime(mappedSlot.getEndTime());
                }
                return response;
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

                SchoolClass studentClass = classRepository
                                .findOne(SchoolClassSpecification.activeById(request.getClassId()))
                                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

                Subject subject = subjectRepository.findOne(SubjectSpecification.activeById(request.getSubjectId()))
                                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

                Teacher teacher = teacherRepository.findOne(TeacherSpecification.activeById(request.getTeacherId()))
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
                ClassRoutine routine = routineRepository.findOne(RoutineSpecification.activeById(id))
                                .orElseThrow(() -> new RuntimeException("Routine slot not found"));

                routineRepository.delete(routine);
        }
}
