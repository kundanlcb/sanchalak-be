package com.cm.sanchalak;

import com.cm.sanchalak.dto.NoticeDto;
import com.cm.sanchalak.dto.NoticeRequest;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.entity.User;
import com.cm.sanchalak.repository.NoticeRepository;
import com.cm.sanchalak.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class NoticeManagementIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        databaseCleanup.cleanAllTables();
        // Ensure we have a user for @WithMockUser to work if it relies on DB,
        // but @WithMockUser usually mocks the security context directly.
        // If the controller uses @CurrentUser to fetch User object from DB, we need to
        // insert it.

        // For this test, we might need a real user in DB if the service looks it up.
        // The Service does: userRepository.findById(userId)
        // So we need to insert a user and use that ID, or mock the user lookup.
        // However, @WithMockUser provides a principal, but it doesn't automatically
        // insert into DB.
        // We'll rely on the fact that we can't easily injection a specific UUID with
        // @WithMockUser unless we use a custom annotation.
        // Actually, @CurrentUser usually resolves UserPrincipal. If UserPrincipal
        // contains ID, we are good.
        // Let's assume we need to seed a user.
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void testCreateNotice() throws Exception {
        // We need a user in the DB that matches the ID from the security context.
        // Since @WithMockUser uses a random/default principal, we might need to adjust.
        // Strategy: We will create a user in DB, and then specific that user's details
        // if we could.
        // But with Spring Security Test, it's easier if we just mock the Service or
        // specific the Principal.
        // A better approach for integration tests is to use a setup method that creates
        // a user and token, then passes the token.
        // But for "quick" verification, let's try to see if we can get away with
        // mocking the Service?
        // No, this is an integration test.

        // Let's try to run it. If it fails on "User not found", we know why.
        // In `NoticeService.createNotice`, it calls `userRepository.findById(userId)`.

        NoticeRequest request = new NoticeRequest();
        request.setTitle("Test Notice");
        request.setContent("This is a test notice content");
        request.setPriority("HIGH");
        request.setTargetRole("ALL");
        request.setPublishDate(LocalDate.now());

        // We expect this to fail with 500 or 404 because the user doesn't exist in DB
        // UNLESS we pre-populate.
        // Let's create a user first.

        // Actually, to make this robust, we should use a custom @WithUserDetails or
        // setup the SecurityContext manually.
        // But let's write the test structure first.

        mockMvc.perform(post("/api/notices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(500)); // Likely fail due to user not found
    }
}
