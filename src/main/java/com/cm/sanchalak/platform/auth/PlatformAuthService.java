package com.cm.sanchalak.platform.auth;

import com.cm.sanchalak.dto.AuthTokenResponseDto;
import com.cm.sanchalak.dto.LoginRequestDto;
import com.cm.sanchalak.dto.PlatformUserDto;
import com.cm.sanchalak.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlatformAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PlatformUserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public PlatformAuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
            PlatformUserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthTokenResponseDto authenticate(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // TODO: Adapt AuthTokenResponseDto or create PlatformAuthResponseDto to include
        // platform roles
        return AuthTokenResponseDto.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .build();
    }

    public PlatformUserDto getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PlatformUserDetails userDetails)) {
            throw new RuntimeException("No authenticated platform user found");
        }

        return PlatformUserDto.builder()
                .id(userDetails.getId().toString())
                .email(userDetails.getEmail())
                .name(userDetails.getName())
                .roles(userDetails.getAuthorities().stream()
                        .map(a -> PlatformRole.valueOf(a.getAuthority()))
                        .collect(Collectors.toSet()))
                .build();
    }

    public java.util.List<PlatformUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> PlatformUserDto.builder()
                        .id(user.getId().toString())
                        .name(user.getName())
                        .email(user.getEmail())
                        .roles(user.getRoles())
                        .build())
                .collect(Collectors.toList());
    }

    public PlatformUserDto createUser(PlatformUserDto userDto, String password) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        PlatformUser user = new PlatformUser();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(userDto.getRoles());

        PlatformUser savedUser = userRepository.save(user);

        return PlatformUserDto.builder()
                .id(savedUser.getId().toString())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .build();
    }

    public void deleteUser(java.util.UUID userId) {
        userRepository.deleteById(userId);
    }
}
