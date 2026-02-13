package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.ApiResult;
import com.cm.sanchalak.dto.JwtAuthenticationResponse;
import com.cm.sanchalak.dto.LoginRequest;
import com.cm.sanchalak.dto.SignUpRequest;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.exception.AppException;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return new JwtAuthenticationResponse(jwt);
    }

    @Transactional
    public ApiResult<String> registerUser(SignUpRequest signUpRequest) {
        if(userRepository.existsByEmail(signUpRequest.email())) {
            return ApiResult.error("EMAIL_EXISTS", "Email Address already in use!");
        }

        User user = new User(signUpRequest.name(),
                signUpRequest.email(),
                signUpRequest.password());

        user.setPassword(passwordEncoder.encode(signUpRequest.password()));

        RoleName roleName = RoleName.ROLE_STUDENT;
        if (signUpRequest.role() != null) {
            try {
                roleName = RoleName.valueOf(signUpRequest.role());
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        
        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException("User Role not set."));
        
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);

        return ApiResult.success("User registered successfully");
    }
}
