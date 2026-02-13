package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.TeacherRequest;
import com.cm.sanchalak.dto.TeacherResponse;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.entity.Teacher;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.SubjectRepository;
import com.cm.sanchalak.repository.TeacherRepository;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.repository.ClassSubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final com.cm.sanchalak.repository.ClassRoutineRepository classRoutineRepository;

    @Autowired
    public TeacherService(TeacherRepository teacherRepository, UserRepository userRepository,
                          RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                          SubjectRepository subjectRepository,
                          ClassSubjectRepository classSubjectRepository,
                          com.cm.sanchalak.repository.ClassRoutineRepository classRoutineRepository) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.subjectRepository = subjectRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.classRoutineRepository = classRoutineRepository;
    }

    public TeacherResponse createTeacher(TeacherRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email address already in use.");
        }

        // Create User
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode("Teacher@123")); // Default password

        Role userRole = roleRepository.findByName(RoleName.ROLE_TEACHER)
                .orElseThrow(() -> new RuntimeException("User Role not set."));
        user.setRoles(Collections.singleton(userRole));

        user = userRepository.save(user);

        // Create Teacher
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setName(request.getName());
        teacher.setEmail(request.getEmail());
        teacher.setPhone(request.getPhone());
        teacher.setQualification(request.getQualification());
        teacher.setProfileImage(request.getProfileImage());

        if (request.getSpecializationIds() != null && !request.getSpecializationIds().isEmpty()) {
            List<Subject> subjects = subjectRepository.findAllById(request.getSpecializationIds());
            teacher.setSpecializations(new HashSet<>(subjects));
        }

        Teacher savedTeacher = teacherRepository.save(teacher);
        return new TeacherResponse(savedTeacher);
    }

    public TeacherResponse updateTeacher(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (!teacher.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email address already in use.");
        }

        teacher.setName(request.getName());
        teacher.setEmail(request.getEmail());
        teacher.setPhone(request.getPhone());
        teacher.setQualification(request.getQualification());
        teacher.setProfileImage(request.getProfileImage());

        if (request.getSpecializationIds() != null) {
            List<Subject> subjects = subjectRepository.findAllById(request.getSpecializationIds());
            teacher.setSpecializations(new HashSet<>(subjects));
        }

        // Update User info as well
        User user = teacher.getUser();
        if (user != null) {
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }

        Teacher updatedTeacher = teacherRepository.save(teacher);
        return new TeacherResponse(updatedTeacher);
    }

    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        // Check dependencies (Assignments)
        if (classSubjectRepository.existsByTeacherId(id)) {
            throw new RuntimeException("Cannot delete teacher assigned to classes.");
        }
        
        // Check dependencies (Routine)
        if (classRoutineRepository.existsByTeacherId(id)) {
            throw new RuntimeException("Cannot delete teacher who is active in the class routine.");
        }
        
        // Soft delete
        teacher.setDeleted(true);
        teacherRepository.save(teacher);
        
        // Disable user login
        User user = teacher.getUser();
        if (user != null) {
            // In a real app we might have an 'active' flag on User, 
            // for now we'll just leave the user account but maybe scramble the password or remove roles
            // Implementing soft delete on User is out of scope for this immediate task but recommended.
        }
    }

    public List<TeacherResponse> getAllTeachers() {
        return teacherRepository.findByDeletedFalse().stream()
                .map(TeacherResponse::new)
                .collect(Collectors.toList());
    }

    public TeacherResponse getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return new TeacherResponse(teacher);
    }
}
