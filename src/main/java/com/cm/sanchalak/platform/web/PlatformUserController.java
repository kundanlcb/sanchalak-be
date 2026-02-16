package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.dto.PlatformUserDto;
import com.cm.sanchalak.platform.auth.PlatformAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/users")
public class PlatformUserController {

    private final PlatformAuthService authService;

    public PlatformUserController(PlatformAuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<PlatformUserDto>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<PlatformUserDto> createUser(@Valid @RequestBody CreatePlatformUserRequest request) {
        // Map request to DTO
        PlatformUserDto userDto = PlatformUserDto.builder()
                .name(request.name())
                .email(request.email())
                .roles(request.roles())
                .build();
        return ResponseEntity.ok(authService.createUser(userDto, request.password()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Inner DTO for creation request
    public record CreatePlatformUserRequest(
            String name,
            String email,
            String password,
            java.util.Set<com.cm.sanchalak.platform.auth.PlatformRole> roles) {
    }
}
