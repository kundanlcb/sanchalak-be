package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.OtpRequestDto;
import com.cm.sanchalak.dto.OtpVerifyDto;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.HashSet;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MobileAuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private final String testMobile = "8877665544";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRepository.findByMobileNumber(testMobile).ifPresent(userRepository::delete);

        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_STUDENT)));

        User user = new User();
        user.setName("Auth Test User");
        user.setEmail("authtest@example.com");
        user.setMobileNumber(testMobile);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(new HashSet<>(Collections.singletonList(studentRole)));
        userRepository.save(user);
    }

    @Test
    void testRequestOtp_Success() throws Exception {
        OtpRequestDto dto = new OtpRequestDto(testMobile);

        mockMvc.perform(post("/api/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRequestOtp_UserNotFound() throws Exception {
        OtpRequestDto dto = new OtpRequestDto("0000000000");

        mockMvc.perform(post("/api/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
    }

    @Test
    void testVerifyOtp_InvalidOtp() throws Exception {
        OtpVerifyDto dto = new OtpVerifyDto(testMobile, "000000", "device-1", "ANDROID");

        mockMvc.perform(post("/api/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_OTP"));
    }

    @Test
    void testRequestOtp_InvalidMobileNumber() throws Exception {
        // 5-digit number should fail validation
        String invalidJson = "{\"mobileNumber\": \"12345\"}";

        mockMvc.perform(post("/api/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
