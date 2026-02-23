package com.cm.sanchalak.config;

import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Set;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() {
        // Seed School Roles
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        });
    }

    private void seedAdminUser() {
        if (userRepository.findByEmail("admin@jan.in").isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();

            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@jan.in");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setMobileNumber("9999999999");
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
            System.out.println("Default application admin created successfully: admin@jan.in / password");
        }
    }
}
