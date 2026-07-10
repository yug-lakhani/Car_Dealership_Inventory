package com.dealership.inventory.controller;

import com.dealership.inventory.dto.request.RegisterRequest;
import com.dealership.inventory.dto.response.RegisterResponse;
import com.dealership.inventory.exception.EmailAlreadyExistsException;
import com.dealership.inventory.security.JwtAuthFilter;
import com.dealership.inventory.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link AuthController}. The security filter chain is
 * disabled here so the focus stays on request mapping, validation, and
 * exception handling; end-to-end security behaviour is covered separately.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // JwtAuthFilter implements Filter, which @WebMvcTest auto-detects and
    // tries to instantiate for real; mocking it here avoids pulling in its
    // JwtService/UserDetailsService dependencies. addFilters = false above
    // means it wouldn't run against requests anyway.
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void register_returns201Created_withRegisteredUser_whenRequestIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest("john.doe", "john.doe@example.com", "SecurePass123");
        RegisterResponse response = new RegisterResponse(
                1L, "john.doe", "john.doe@example.com", "USER", Instant.parse("2026-01-01T00:00:00Z"));

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_returns400BadRequest_whenEmailIsInvalid() throws Exception {
        String invalidPayload = """
                {"username":"john.doe","email":"not-an-email","password":"SecurePass123"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void register_returns400BadRequest_whenPasswordIsTooShort() throws Exception {
        String invalidPayload = """
                {"username":"john.doe","email":"john.doe@example.com","password":"short"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void register_returns400BadRequest_whenUsernameIsBlank() throws Exception {
        String invalidPayload = """
                {"username":"","email":"john.doe@example.com","password":"SecurePass123"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void register_returns409Conflict_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("john.doe", "john.doe@example.com", "SecurePass123");
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("john.doe@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_returns400BadRequest_whenPasswordFieldIsMissingEntirely() throws Exception {
        String payloadWithNoPasswordKey = "{\"username\":\"john.doe\",\"email\":\"john.doe@example.com\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithNoPasswordKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").value("Password is required"));

        verifyNoInteractions(authService);
    }

    @Test
    void register_returns400BadRequest_whenRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void register_returns400BadRequest_whenBodyIsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-valid-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
