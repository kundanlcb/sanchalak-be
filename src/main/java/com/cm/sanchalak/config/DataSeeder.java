package com.cm.sanchalak.config;

import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        seedRoles();
    }

    private void seedRoles() {
        // Seed School Roles
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        });
    }
}
