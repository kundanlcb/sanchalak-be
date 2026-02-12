package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.ApiResponse;
import com.cm.sanchalak.dto.JwtAuthenticationResponse;
import com.cm.sanchalak.dto.LoginRequest;
import com.cm.sanchalak.dto.SignUpRequest;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    com.cm.sanchalak.repository.RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return new JwtAuthenticationResponse(jwt);
    }

    @Transactional
    public ApiResponse registerUser(SignUpRequest signUpRequest) {
        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ApiResponse(false, "Email Address already in use!");
        }

        User user = new User(signUpRequest.getName(),
                signUpRequest.getEmail(),
                signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        RoleName roleName = RoleName.ROLE_STUDENT;
        if (signUpRequest.getRole() != null) {
            try {
                roleName = RoleName.valueOf("ROLE_" + signUpRequest.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        
        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new com.cm.sanchalak.exception.AppException("User Role not set."));
        
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);

        return new ApiResponse(true, "User registered successfully");
    }
}
