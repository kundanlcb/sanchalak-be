package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.ApiResult;
import com.cm.sanchalak.dto.AuthTokenResponseDto;
import com.cm.sanchalak.dto.LoginRequest;
import com.cm.sanchalak.dto.SignUpRequest;
import com.cm.sanchalak.dto.UserProfileDto;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.cm.sanchalak.platform.school.SchoolUserRepository;
import com.cm.sanchalak.platform.subscription.SubscriptionService;
import com.cm.sanchalak.platform.subscription.Feature;
import com.cm.sanchalak.platform.school.SchoolPermissionRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    private final RefreshTokenService refreshTokenService;
    private final SchoolUserRepository schoolUserRepository;
    private final SubscriptionService subscriptionService;
    private final SchoolPermissionRepository schoolPermissionRepository;

    @org.springframework.beans.factory.annotation.Value("${app.jwt.access-token-expiry-ms:900000}")
    private Long accessTokenExpiryMs;

    public AuthTokenResponseDto authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new AppException("User not found"));

        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(user, "WEB", "BROWSER");

        String fullName = user.getName();
        String firstName = fullName;
        String lastName = "";

        if (fullName != null && fullName.contains(" ")) {
            int lastSpaceIndex = fullName.lastIndexOf(" ");
            firstName = fullName.substring(0, lastSpaceIndex);
            lastName = fullName.substring(lastSpaceIndex + 1);
        }

        // Resolve permissions based on school subscription and role-specific overrides
        List<String> permissions = schoolUserRepository.findByUserId(user.getId())
                .map(schoolUser -> {
                    UUID schoolId = schoolUser.getSchoolId();
                    List<String> planFeatures = subscriptionService.getActiveSubscription(schoolId)
                            .getPlan().getFeatures().stream()
                            .map(Feature::getCode)
                            .collect(Collectors.toList());

                    // Check for role-specific overrides
                    List<String> roleOverrides = schoolPermissionRepository.findBySchoolId(schoolId).stream()
                            .filter(p -> p.getRoleName() == user.getRoles().iterator().next().getName())
                            .map(p -> p.getFeatureCode())
                            .collect(Collectors.toList());

                    // If overrides exist, return them; otherwise, return all plan features
                    return roleOverrides.isEmpty() ? planFeatures : roleOverrides;
                })
                .orElse(Collections.emptyList());

        UserProfileDto userProfile = UserProfileDto.builder()
                .userId(user.getId())
                .mobileNumber(user.getMobileNumber())
                .email(user.getEmail())
                .name(fullName)
                .firstName(firstName)
                .lastName(lastName)
                .role(user.getRoles().iterator().next().getName().name())
                .permissions(permissions)
                .build();

        return AuthTokenResponseDto.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiryMs / 1000)
                .user(userProfile)
                .build();
    }

    @Transactional
    public ApiResult<String> registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.email())) {
            return ApiResult.error("EMAIL_EXISTS", "Email Address already in use!");
        }

        User user = User.builder()
                .name(signUpRequest.name())
                .email(signUpRequest.email())
                .password(signUpRequest.password())
                .build();

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
