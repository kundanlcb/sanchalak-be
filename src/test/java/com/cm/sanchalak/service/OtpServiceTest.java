package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.OtpVerification;
import com.cm.sanchalak.repository.OtpVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OtpServiceTest {

    @Mock
    private OtpVerificationRepository otpRepository;
    
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(otpService, "otpExpiryMinutes", 5);
        ReflectionTestUtils.setField(otpService, "maxOtpRequests", 3);
        ReflectionTestUtils.setField(otpService, "rateLimitWindowMinutes", 15);
        ReflectionTestUtils.setField(otpService, "encryptionKey", "1234567812345678"); // 16 bytes
        
        // Mock default behavior for encryption
        // Ideally we should test with real encryption logic if possible, 
        // or ensure the service uses injected dependencies for encryption.
        // For now, assuming internal encryption works as we set the key.
    }

    @Test
    void testGenerateOtp_Success() {
        // Mock countRecentOtps instead of countByMobileNumber...
        when(otpRepository.countRecentOtps(anyString(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(otpRepository.save(any(OtpVerification.class))).thenAnswer(i -> i.getArguments()[0]);

        String otp = otpService.generateOtp("9876543210");

        assertNotNull(otp);
        assertEquals(6, otp.length());
        verify(otpRepository, times(1)).save(any(OtpVerification.class));
        verify(auditLogService, times(1)).logAction(isNull(), eq("OTP_GENERATED"), eq("MOBILE_NUMBER"), eq("9876543210"), anyString(), isNull(), isNull(), eq("SUCCESS"));
    }

    @Test
    void testValidateOtp_Success() {
        String testOtp = "123456";
        // We need to know how the service encrypts to mock the db record correctly OR trust the decrypt logic
        // Since `decryptOtp` is private, we rely on `generateOtp` logic or we can try to call encrypt if accessible via reflection?
        // Or cleaner: create a verification record and inject encrypted OTP manually if we knew the algorithm.
        // Given the service uses standard AES, we'll try to simulate a full flow if possible, or just trust the helper.
        
        // For unit test simplicity, let's assume encryption works.
        // But wait, validation decrypts the stored OTP. If we store dummy "ENCRYPTED_123456", decrypt will fail or produce garbage.
        // We really need to encrypt "123456" using the same key.
        // Let's create a helper method in test to encrypt using the same logic if possible or make the encryption method protected.
        // Alternatively, use Reflection to invoke encrypt.
        
        String encryptedOtp = ReflectionTestUtils.invokeMethod(otpService, "encryptOtp", testOtp);
        
        OtpVerification verification = new OtpVerification();
        verification.setMobileNumber("9876543210");
        verification.setOtpCode(encryptedOtp);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verification.setIsUsed(false);
        verification.setAttemptCount(0);

        when(otpRepository.findByMobileNumberAndIsUsedFalseAndExpiresAtAfter(eq("9876543210"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(verification));

        boolean isValid = otpService.validateOtp("9876543210", testOtp);

        assertTrue(isValid);
        assertTrue(verification.getIsUsed());
        verify(otpRepository, times(2)).save(verification); // Once for attempt increment, once for success
        verify(auditLogService, times(1)).logAction(isNull(), eq("OTP_VERIFIED"), anyString(), anyString(), anyString(), isNull(), isNull(), eq("SUCCESS"));
    }

    @Test
    void testValidateOtp_Failure_InvalidOtp() {
        String correctOtp = "123456";
        String wrongOtp = "654321";
        
        String encryptedOtp = ReflectionTestUtils.invokeMethod(otpService, "encryptOtp", correctOtp);
        
        OtpVerification verification = new OtpVerification();
        verification.setMobileNumber("9876543210");
        verification.setOtpCode(encryptedOtp);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verification.setIsUsed(false);
        verification.setAttemptCount(0);

        when(otpRepository.findByMobileNumberAndIsUsedFalseAndExpiresAtAfter(eq("9876543210"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(verification));

        boolean isValid = otpService.validateOtp("9876543210", wrongOtp);

        assertFalse(isValid);
        assertFalse(verification.getIsUsed());
        assertEquals(1, verification.getAttemptCount()); // Incremented attempt
        verify(otpRepository, times(1)).save(verification);
        verify(auditLogService, times(1)).logAction(isNull(), eq("OTP_FAILED"), anyString(), anyString(), contains("verification failed"), isNull(), isNull(), eq("FAILURE"));
    }
    
    @Test
    void testValidateOtp_Failure_MaxAttempts() {
        OtpVerification verification = new OtpVerification();
        verification.setMobileNumber("9876543210");
        verification.setAttemptCount(5); // Max attempts reached

        when(otpRepository.findByMobileNumberAndIsUsedFalseAndExpiresAtAfter(eq("9876543210"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(verification));

        boolean isValid = otpService.validateOtp("9876543210", "123456");

        assertFalse(isValid);
        verify(auditLogService, times(1)).logAction(isNull(), eq("OTP_LOCKED"), anyString(), anyString(), anyString(), isNull(), isNull(), eq("FAILURE"));
    }
}
