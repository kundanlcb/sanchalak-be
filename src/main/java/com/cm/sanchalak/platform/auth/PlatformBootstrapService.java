package com.cm.sanchalak.platform.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PlatformBootstrapService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PlatformBootstrapService.class);

    private final PlatformUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PlatformBootstrapService(PlatformUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            logger.info("No platform users found. Creating default admin user.");

            PlatformUser defaultAdmin = new PlatformUser();
            defaultAdmin.setName("System Admin");
            defaultAdmin.setEmail("admin@sanchalak.in");
            // Set password to 'password' securely hashed
            defaultAdmin.setPassword(passwordEncoder.encode("password"));
            defaultAdmin.setRoles(Set.of(PlatformRole.OWNER));

            userRepository.save(defaultAdmin);
            logger.info("Default admin user created successfully: admin@sanchalak.in / password");
        } else {
            logger.info("Platform users already exist. Skipping admin bootstrap.");
        }
    }
}
