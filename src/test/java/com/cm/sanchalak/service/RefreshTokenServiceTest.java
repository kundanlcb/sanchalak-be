package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.RefreshToken;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;
    
    // We utilize the static encoder from service or create a new one since algorithm is standard
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiryDays", 30);
    }

    @Test
    void testCreateRefreshToken() {
        User user = new User();
        user.setId(UUID.randomUUID());

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        String token = refreshTokenService.createRefreshToken(user, "device-123", "android");

        assertNotNull(token);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testRotateRefreshToken_Success() {
        String plainToken = "some-plain-token";
        String hashedToken = passwordEncoder.encode(plainToken);
        
        User user = new User();
        user.setId(UUID.randomUUID());
        
        RefreshToken oldToken = new RefreshToken();
        oldToken.setId(10L);
        oldToken.setUser(user);
        oldToken.setTokenHash(hashedToken);
        oldToken.setTokenFamilyId(UUID.randomUUID());
        oldToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        oldToken.setIsRevoked(false);
        oldToken.setLastUsedAt(null); // Not used yet

        // Mock findAll because service iterates all tokens (inefficient implementation)
        when(refreshTokenRepository.findAll()).thenReturn(Collections.singletonList(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        String newToken = refreshTokenService.rotateRefreshToken(plainToken);

        assertNotNull(newToken);
        assertNotEquals(plainToken, newToken);
        
        // Check old token is marked used and revoked
        assertNotNull(oldToken.getLastUsedAt());
        assertTrue(oldToken.getIsRevoked()); // Service calls .revoke() which likely sets IsRevoked=true
        
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class)); // 1 for old update, 1 for new create
    }

    @Test
    void testRotateRefreshToken_ReuseDetection() {
        String plainToken = "stolen-token";
        String hashedToken = passwordEncoder.encode(plainToken);
        
        User user = new User();
        user.setId(UUID.randomUUID());
        
        RefreshToken stolenToken = new RefreshToken();
        stolenToken.setId(11L);
        stolenToken.setUser(user);
        stolenToken.setTokenHash(hashedToken);
        stolenToken.setTokenFamilyId(UUID.randomUUID());
        stolenToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        stolenToken.setIsRevoked(false);
        stolenToken.setLastUsedAt(LocalDateTime.now().minusMinutes(5)); // Already used!

        when(refreshTokenRepository.findAll()).thenReturn(Collections.singletonList(stolenToken));

        assertThrows(SecurityException.class, () -> {
            refreshTokenService.rotateRefreshToken(plainToken);
        });
        
        verify(refreshTokenRepository, times(1)).revokeTokenFamily(eq(stolenToken.getTokenFamilyId()), any(LocalDateTime.class));
    }

    @Test
    void testValidateToken_Success() {
        String plainToken = "valid-token";
        String hashedToken = passwordEncoder.encode(plainToken);
        
        User user = new User();
        user.setId(UUID.randomUUID());
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashedToken);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        refreshToken.setIsRevoked(false);

        when(refreshTokenRepository.findAll()).thenReturn(Collections.singletonList(refreshToken));

        Optional<User> result = refreshTokenService.validateToken(plainToken);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }
}
