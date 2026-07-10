package com.dealership.inventory.integration;

import com.dealership.inventory.dto.request.LoginRequest;
import com.dealership.inventory.entity.Role;
import com.dealership.inventory.entity.User;
import com.dealership.inventory.repository.UserRepository;
import com.dealership.inventory.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack test for {@code POST /api/auth/login}: real Spring Security
 * filter chain, real JPA repository, real {@link JwtService}, and an
 * embedded H2 database (see {@code application-test.yml}).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthLoginIntegrationTest {

    private static final String RAW_PASSWORD = "SecurePass123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void seedExistingUser() {
        userRepository.save(User.builder()
                .username("john.doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .role(Role.USER)
                .build());
    }

    @Test
    void login_returns200Ok_withValidJwtAndUserInfo_whenCredentialsAreCorrect() throws Exception {
        LoginRequest request = new LoginRequest("john.doe@example.com", RAW_PASSWORD);

        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(responseBody).get("token").asText();
        assertThat(jwtService.extractUsername(token)).isEqualTo("john.doe");
    }

    @Test
    void login_returns401Unauthorized_whenEmailDoesNotExist() throws Exception {
        LoginRequest request = new LoginRequest("nobody@example.com", RAW_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returns401Unauthorized_whenPasswordIsIncorrect() throws Exception {
        LoginRequest request = new LoginRequest("john.doe@example.com", "WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returns400BadRequest_whenPayloadFailsValidation() throws Exception {
        String invalidPayload = "{\"email\":\"not-an-email\",\"password\":\"\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }
}
