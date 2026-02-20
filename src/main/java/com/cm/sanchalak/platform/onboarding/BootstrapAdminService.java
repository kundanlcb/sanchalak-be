package com.cm.sanchalak.platform.onboarding;

import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.User;

import com.cm.sanchalak.platform.school.SchoolRepository;
import com.cm.sanchalak.platform.school.SchoolUser;
import com.cm.sanchalak.platform.school.SchoolUserRepository;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
public class BootstrapAdminService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SchoolUserRepository schoolUserRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapAdminService(SchoolRepository schoolRepository, UserRepository userRepository,
            RoleRepository roleRepository, SchoolUserRepository schoolUserRepository, PasswordEncoder passwordEncoder) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.schoolUserRepository = schoolUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User bootstrapAdmin(UUID schoolId, BootstrapAdminRequest request) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new RuntimeException("School not found");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email address already in use!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobileNumber(request.getMobileNumber());

        Role adminRole = roleRepository.findByName(RoleName.ROLE_SCHOOL_ADMIN)
                .orElseThrow(() -> new RuntimeException("User Role not set."));

        user.setRoles(Collections.singleton(adminRole));

        User savedUser = userRepository.save(user);

        // Link user to school
        SchoolUser schoolUser = new SchoolUser(schoolId, savedUser.getId());
        schoolUserRepository.save(schoolUser);

        return savedUser;
    }
}
