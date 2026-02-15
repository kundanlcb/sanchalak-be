package com.cm.sanchalak.platform.web;

import com.cm.sanchalak.platform.support.SupportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/v1/support")
public class SupportController {

    private final SupportService service;

    public SupportController(SupportService service) {
        this.service = service;
    }

    @PostMapping("/unlock/{userId}")
    public ResponseEntity<Void> unlockAccount(@PathVariable UUID userId) {
        service.unlockAccount(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/impersonate")
    public ResponseEntity<Map<String, String>> impersonateUser(@RequestParam String email) {
        String token = service.impersonateUser(email);
        return ResponseEntity.ok(Map.of("accessToken", token, "tokenType", "Bearer"));
    }
}
