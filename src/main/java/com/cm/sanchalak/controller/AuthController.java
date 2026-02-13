package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.*;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.security.JwtTokenProvider;
import com.cm.sanchalak.security.UserPrincipal;
import com.cm.sanchalak.service.AuthService;
import com.cm.sanchalak.service.OtpService;
import com.cm.sanchalak.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Unified authentication controller for web and mobile
 * Supports: Email/password login, OTP login, token refresh
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication (Email/Password & Mobile OTP)")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final OtpService otpService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${app.jwt.access-token-expiry-ms:900000}")
    private Long accessTokenExpiryMs;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthenticationResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        ApiResult<String> response = authService.registerUser(signUpRequest);

        if(response.success()) {
             URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(signUpRequest.email()).toUri();
             return ResponseEntity.created(location).body(response);
        } else {
             return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ========== OTP-based Authentication (for mobile apps) ==========
    
    /**
     * Request OTP for mobile number
     * POST /api/auth/otp/request
     */
    @Operation(summary = "Request OTP for mobile login", description = "Generates and sends a 6-digit OTP to the registered mobile number")
    @ApiResponse(responseCode = "200", description = "OTP sent successfully")
    @ApiResponse(responseCode = "400", description = "User not found or invalid request", content = @Content)
    @ApiResponse(responseCode = "429", description = "Rate limit exceeded (max 3 attempts per 15 min)", content = @Content)
    @PostMapping("/otp/request")
    public ResponseEntity<ApiResult<String>> requestOtp(
            @Valid @RequestBody OtpRequestDto request,
            HttpServletRequest httpRequest) {
        
        String mobileNumber = request.mobileNumber();
        String clientIp = getClientIp(httpRequest);
        
        logger.info("OTP request for mobile: {}, IP: {}", maskMobile(mobileNumber), clientIp);
        
        try {
            // Check if user with mobile number exists
            Optional<User> userOpt = userRepository.findByMobileNumber(mobileNumber);
            if (userOpt.isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResult.error("USER_NOT_FOUND", "No account found with this mobile number"));
            }
            
            // Generate and send OTP
            String otp = otpService.generateOtp(mobileNumber);
            
            // In production, integrate SMS gateway here
            // For development, return OTP in response (REMOVE IN PRODUCTION)
            logger.info("OTP generated: {} for mobile: {}", otp, maskMobile(mobileNumber));
            
            return ResponseEntity.ok(ApiResult.success(
                "OTP sent successfully. Valid for 5 minutes. [DEV: OTP=" + otp + "]"
            ));
            
        } catch (IllegalStateException e) {
            // Rate limiting error
            return ResponseEntity
                .status(429)
                .body(ApiResult.error("RATE_LIMIT_EXCEEDED", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to generate OTP for mobile: {}", maskMobile(mobileNumber), e);
            return ResponseEntity
                .internalServerError()
                .body(ApiResult.error("OTP_GENERATION_FAILED", "Failed to send OTP. Please try again."));
        }
    }
    
    /**
     * Verify OTP and issue JWT tokens
     * POST /api/auth/otp/verify
     */
    @Operation(summary = "Verify Mobile OTP", description = "Validates the OTP and returns JWT access/refresh tokens. Used for mobile app login.")
    @ApiResponse(responseCode = "200", description = "Login successful, tokens issued")
    @ApiResponse(responseCode = "400", description = "Invalid OTP or User not found", content = @Content)
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResult<AuthTokenResponseDto>> verifyOtp(
            @Valid @RequestBody OtpVerifyDto request,
            HttpServletRequest httpRequest) {
        
        String mobileNumber = request.mobileNumber();
        String otp = request.otp();
        String clientIp = getClientIp(httpRequest);
        
        logger.info("OTP verification for mobile: {}, IP: {}", maskMobile(mobileNumber), clientIp);
        
        // Validate OTP
        boolean isValid = otpService.validateOtp(mobileNumber, otp);
        if (!isValid) {
            return ResponseEntity
                .badRequest()
                .body(ApiResult.error("INVALID_OTP", "Invalid or expired OTP"));
        }
        
        // Get user
        Optional<User> userOpt = userRepository.findByMobileNumber(mobileNumber);
        if (userOpt.isEmpty()) {
            return ResponseEntity
                .badRequest()
                .body(ApiResult.error("USER_NOT_FOUND", "No account found with this mobile number"));
        }
        
        User user = userOpt.get();
        
        // Create authentication object with UserPrincipal
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.getAuthorities()
        );
        
        // Generate JWT access token
        String accessToken = jwtTokenProvider.generateToken(authentication);
        
        // Generate refresh token
        String refreshToken = refreshTokenService.createRefreshToken(
            user, 
            request.deviceId(), 
            request.deviceType()
        );
        
        // Build user profile
        AuthTokenResponseDto.UserProfileDto userProfile = AuthTokenResponseDto.UserProfileDto.builder()
            .userId(user.getId().toString())
            .mobileNumber(user.getMobileNumber())
            .email(user.getEmail())
            .firstName(user.getName())
            .lastName("")
            .role(user.getRoles().iterator().next().getName().name())
            .build();
        
        // Build token response
        AuthTokenResponseDto response = AuthTokenResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(accessTokenExpiryMs / 1000)
            .user(userProfile)
            .build();
        
        logger.info("JWT tokens issued for user: {}, mobile: {}, role: {}", 
            user.getId(), maskMobile(mobileNumber), userProfile.getRole());
        
        return ResponseEntity.ok(ApiResult.success(response));
    }
    
    /**
     * Refresh access token using refresh token
     * POST /api/auth/refresh
     */
    @Operation(summary = "Refresh Access Token", description = "Exchanges a valid refresh token for a new access token and rotated refresh token.")
    @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid refresh token or Reuse attempt detected", content = @Content)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<AuthTokenResponseDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request,
            HttpServletRequest httpRequest) {
        
        String refreshToken = request.getRefreshToken();
        String clientIp = getClientIp(httpRequest);
        
        logger.info("Token refresh request from IP: {}", clientIp);
        
        try {
            // Validate and rotate refresh token
            String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
            
            // Get user from token
            Optional<User> userOpt = refreshTokenService.validateToken(newRefreshToken);
            if (userOpt.isEmpty()) {
                return ResponseEntity
                    .status(401)
                    .body(ApiResult.error("INVALID_TOKEN", "Invalid refresh token"));
            }
            
            User user = userOpt.get();
            
            // Create authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                    .collect(Collectors.toList())
            );
            
            // Generate new JWT access token
            String accessToken = jwtTokenProvider.generateToken(authentication);
            
            // Build user profile
            AuthTokenResponseDto.UserProfileDto userProfile = AuthTokenResponseDto.UserProfileDto.builder()
                .userId(user.getId().toString())
                .mobileNumber(user.getMobileNumber())
                .email(user.getEmail())
                .firstName(user.getName())
                .lastName("")
                .role(user.getRoles().iterator().next().getName().name())
                .build();
            
            // Build token response
            AuthTokenResponseDto response = AuthTokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiryMs / 1000)
                .user(userProfile)
                .build();
            
            logger.info("Tokens refreshed for user: {}", user.getId());
            
            return ResponseEntity.ok(ApiResult.success(response));
            
        } catch (SecurityException e) {
            // Token reuse detected
            logger.error("Token reuse detected: {}", e.getMessage());
            return ResponseEntity
                .status(401)
                .body(ApiResult.error("TOKEN_REUSE_DETECTED", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(401)
                .body(ApiResult.error("INVALID_TOKEN", e.getMessage()));
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            return ResponseEntity
                .internalServerError()
                .body(ApiResult.error("REFRESH_FAILED", "Failed to refresh token"));
        }
    }
    
    /**
     * Logout and revoke refresh token
     * POST /api/auth/logout
     */
    @Operation(summary = "Logout (Mobile)", description = "Revokes the specified refresh token so it cannot be used again.")
    @ApiResponse(responseCode = "200", description = "Logged out successfully")
    @ApiResponse(responseCode = "500", description = "Logout failed", content = @Content)
    @PostMapping("/logout")
    public ResponseEntity<ApiResult<String>> logout(
            @Valid @RequestBody RefreshTokenRequestDto request,
            HttpServletRequest httpRequest) {
        
        String refreshToken = request.getRefreshToken();
        String clientIp = getClientIp(httpRequest);
        
        logger.info("Logout request from IP: {}", clientIp);
        
        try {
            refreshTokenService.revokeToken(refreshToken);
            logger.info("Refresh token revoked successfully");
            
            return ResponseEntity.ok(ApiResult.success("Logged out successfully"));
            
        } catch (Exception e) {
            logger.error("Logout failed", e);
            return ResponseEntity
                .internalServerError()
                .body(ApiResult.error("LOGOUT_FAILED", "Failed to logout"));
        }
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Get client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * Mask mobile number for logging
     */
    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 6) {
            return "***";
        }
        return mobile.substring(0, 3) + "***" + mobile.substring(mobile.length() - 4);
    }
}
