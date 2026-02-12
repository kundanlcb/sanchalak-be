package com.cm.sanchalak;

import com.cm.sanchalak.dto.LoginRequest;
import com.cm.sanchalak.dto.SignUpRequest;
import com.cm.sanchalak.entity.Role;
import com.cm.sanchalak.entity.RoleName;
import com.cm.sanchalak.repository.RoleRepository;
import com.cm.sanchalak.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(this.context).build();
        userRepository.deleteAll();
        
        if (roleRepository.count() == 0) {
            for (RoleName roleName : RoleName.values()) {
                roleRepository.save(new Role(roleName));
            }
        }
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setName("Test User");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setRole("STUDENT");

        webTestClient.post().uri("/api/auth/signup")
            .bodyValue(signUpRequest)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                 JsonNode body = result.getResponseBody();
                 assertThat(body.get("success").asBoolean()).isTrue();
                 assertThat(body.get("message").asText()).isEqualTo("User registered successfully");
            });
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // First register
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setName("Login User");
        signUpRequest.setEmail("login@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setRole("STUDENT");

        webTestClient.post().uri("/api/auth/signup")
            .bodyValue(signUpRequest)
            .exchange()
            .expectStatus().isCreated();

        // Then login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("password");

        webTestClient.post().uri("/api/auth/signin")
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body.get("tokenType").asText()).isEqualTo("Bearer");
            });
    }
}
