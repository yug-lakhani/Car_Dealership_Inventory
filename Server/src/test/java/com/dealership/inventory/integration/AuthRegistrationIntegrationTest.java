package com.dealership.inventory.integration;

import com.dealership.inventory.dto.request.RegisterRequest;
import com.dealership.inventory.entity.User;
import com.dealership.inventory.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack test for {@code POST /api/auth/register}: real Spring Security
 * filter chain, real JPA repository, and an embedded H2 database (see
 * {@code application-test.yml}). Verifies the endpoint's observable
 * behaviour end-to-end rather than any single layer in isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_persistsUserWithHashedPasswordAndUserRole_andReturns201() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "integration.user", "integration.user@example.com", "SecurePass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("WWW-Authenticate"))
                .andExpect(jsonPath("$.username").value("integration.user"))
                .andExpect(jsonPath("$.email").value("integration.user@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        Optional<User> persisted = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals("integration.user@example.com"))
                .findFirst();

        assertThat(persisted).isPresent();
        User savedUser = persisted.get();
        assertThat(savedUser.getPassword()).isNotEqualTo("SecurePass123");
        assertThat(passwordEncoder.matches("SecurePass123", savedUser.getPassword())).isTrue();
    }

    @Test
    void register_returns409Conflict_whenEmailIsAlreadyRegistered() throws Exception {
        RegisterRequest firstRequest = new RegisterRequest(
                "duplicate.user", "duplicate.user@example.com", "SecurePass123");
        RegisterRequest secondRequest = new RegisterRequest(
                "duplicate.user2", "duplicate.user@example.com", "AnotherPass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_returns400BadRequest_whenPayloadFailsValidation() throws Exception {
        String invalidPayload = """
                {"username":"","email":"not-an-email","password":"short"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns400BadRequest_whenPasswordFieldIsMissingEntirely() throws Exception {
        String payloadWithNoPasswordKey = "{\"username\":\"no.password.user\",\"email\":\"no.password.user@example.com\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithNoPasswordKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").value("Password is required"));
    }

    @Test
    void register_returns400BadRequest_whenRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
