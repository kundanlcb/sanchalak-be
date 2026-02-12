package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.ApiResponse;
import com.cm.sanchalak.dto.JwtAuthenticationResponse;
import com.cm.sanchalak.dto.LoginRequest;
import com.cm.sanchalak.dto.SignUpRequest;
import com.cm.sanchalak.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthenticationResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        ApiResponse response = authService.registerUser(signUpRequest);

        if(response.getSuccess()) {
             URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(signUpRequest.getEmail()).toUri();
             return ResponseEntity.created(location).body(response);
        } else {
             return ResponseEntity.badRequest().body(response);
        }
    }
}
