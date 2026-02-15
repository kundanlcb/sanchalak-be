package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.dto.AuthTokenResponseDto;
import com.cm.sanchalak.dto.LoginRequestDto;
import com.cm.sanchalak.platform.auth.PlatformAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/v1/auth")
public class PlatformAuthController {

    private final PlatformAuthService authService;

    public PlatformAuthController(PlatformAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        return ResponseEntity.ok(authService.authenticate(loginRequest));
    }
}
