package com.cm.sanchalak.integration;

import com.cm.sanchalak.dto.ApiResult;
import com.cm.sanchalak.dto.OtpRequestDto;
import com.cm.sanchalak.dto.OtpVerifyDto;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.repository.OtpVerificationRepository;
import com.cm.sanchalak.DatabaseCleanup;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OtpAuthenticationFlowTest {

    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private DatabaseCleanup databaseCleanup;

    private String testMobile = "9988776655";

    @BeforeEach
    void setUp() {
        // Full database cleanup to avoid FK constraints and rate limits
        databaseCleanup.cleanAllTables();
        
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Ensure role exists
        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_STUDENT)));

        // Create user
        User user = new User();
        user.setName("Test Student");
        user.setEmail("teststudent@example.com");
        user.setMobileNumber(testMobile);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(new HashSet<>(Collections.singletonList(studentRole)));
        // user.setIsActive(true);
        
        userRepository.save(user);
    }

    @Test
    void testOtpAuthenticationFlow() throws Exception {
        // 1. Request OTP
        OtpRequestDto requestDto = new OtpRequestDto(testMobile);
        
        MvcResult requestResult = mockMvc.perform(post("/api/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(result -> System.out.println("OTP Request Response: " + result.getResponse().getContentAsString()))
                // .andExpect(status().isOk()) // Don't check status yet to debug
                // .andExpect(jsonPath("$.success").value(true))
                .andReturn();
                
        String responseBody = requestResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        
        if (!jsonNode.path("success").asBoolean()) {
             throw new AssertionError("OTP Request failed: " + responseBody);
        }
        
        String message = jsonNode.get("data").asText();
        
        // Extract OTP from "OTP sent successfully. Valid for 5 minutes. [DEV: OTP=123456]"
        String otp = extractOtpFromMessage(message);
        
        // 2. Verify OTP
        OtpVerifyDto verifyDto = new OtpVerifyDto(testMobile, otp, "device-1", "ANDROID");
        
        MvcResult verifyResult = mockMvc.perform(post("/api/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyDto)))
                .andDo(result -> System.out.println("OTP Verify Response: " + result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn();
                
        String verifyBody = verifyResult.getResponse().getContentAsString();
        JsonNode verifyNode = objectMapper.readTree(verifyBody);
        String accessToken = verifyNode.get("data").get("accessToken").asText();
        
        // Verify tokens are non-empty
        assertNotNull(accessToken);
        assertFalse(accessToken.isEmpty());
        
        String refreshToken = verifyNode.get("data").get("refreshToken").asText();
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }
    
    private String extractOtpFromMessage(String message) {
        // The service now returns just the OTP string in data
        // "OTP sent successfully. Valid for 5 minutes." (message)
        // "123456" (data)
        // In the test flow:
        // String message = jsonNode.get("data").asText();
        // Since my current test is failing on this, let's just use the data field directly if it looks like an OTP
        
        if (message != null && message.matches("\\d{6}")) {
            return message;
        }

        // Expected format fallback: "... [DEV: OTP=123456]"
        if (message.contains("OTP=")) {
            String part = message.split("OTP=")[1];
            return part.substring(0, 6); // Take first 6 chars
        }
        
        throw new RuntimeException("OTP not found in response message or data: " + message);
    }
}
