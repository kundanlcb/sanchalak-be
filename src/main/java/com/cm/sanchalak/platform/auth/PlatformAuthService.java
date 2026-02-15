package com.cm.sanchalak.platform.auth;

import com.cm.sanchalak.dto.AuthTokenResponseDto;
import com.cm.sanchalak.dto.LoginRequestDto;
import com.cm.sanchalak.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
}
