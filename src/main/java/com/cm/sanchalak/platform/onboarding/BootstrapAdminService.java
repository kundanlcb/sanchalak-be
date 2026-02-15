package com.cm.sanchalak.platform.onboarding;

import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.User;

import com.cm.sanchalak.platform.school.SchoolRepository;
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
    private final PasswordEncoder passwordEncoder;

    public BootstrapAdminService(SchoolRepository schoolRepository, UserRepository userRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("User Role not set."));

        user.setRoles(Collections.singleton(adminRole));

        // TODO: Link user to school via SchoolUser or similar mapping entity if it
        // exists
        // For now, assuming user is created within the school's tenant context or
        // globally but assigned to school
        // If sanchalak_be uses a separate schema per tenant or a discriminator, that
        // logic goes here.
        // Assuming a simpler model where User might have a schoolId if multi-tenancy is
        // shared-db-discriminator

        return userRepository.save(user);
    }
}
