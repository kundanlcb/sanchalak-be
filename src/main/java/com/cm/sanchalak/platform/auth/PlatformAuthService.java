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

    public PlatformAuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
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
}
