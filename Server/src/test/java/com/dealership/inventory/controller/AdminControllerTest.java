package com.dealership.inventory.controller;

import com.dealership.inventory.dto.request.RegisterRequest;
import com.dealership.inventory.dto.response.RegisterResponse;
import com.dealership.inventory.exception.EmailAlreadyExistsException;
import com.dealership.inventory.security.JwtAuthFilter;
import com.dealership.inventory.service.AdminService;
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
 * Web-layer tests for {@link AdminController}. The security filter chain
 * (and therefore method-security enforcement of {@code @PreAuthorize}) is
 * disabled here so the focus stays on request mapping, validation, and
 * exception handling; the admin-only restriction is verified end-to-end by
 * {@code AdminRegisterAdminIntegrationTest}.
 */
@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void registerAdmin_returns201Created_withCreatedAdmin_whenRequestIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest("new.admin", "new.admin@example.com", "SecurePass123");
        RegisterResponse response = new RegisterResponse(
                1L, "new.admin", "new.admin@example.com", "ADMIN", Instant.parse("2026-01-01T00:00:00Z"));

        when(adminService.createAdmin(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("new.admin"))
                .andExpect(jsonPath("$.email").value("new.admin@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void registerAdmin_returns400BadRequest_whenEmailIsInvalid() throws Exception {
        String invalidPayload = "{\"username\":\"new.admin\",\"email\":\"not-an-email\",\"password\":\"SecurePass123\"}";

        mockMvc.perform(post("/api/admin/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adminService);
    }

    @Test
    void registerAdmin_returns409Conflict_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("new.admin", "taken@example.com", "SecurePass123");
        when(adminService.createAdmin(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("taken@example.com"));

        mockMvc.perform(post("/api/admin/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
