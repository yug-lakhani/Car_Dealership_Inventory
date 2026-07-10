package com.dealership.inventory.integration;

import com.dealership.inventory.dto.request.RegisterRequest;
import com.dealership.inventory.entity.Role;
import com.dealership.inventory.entity.User;
import com.dealership.inventory.repository.UserRepository;
import com.dealership.inventory.security.JwtService;
import com.dealership.inventory.security.UserDetailsFactory;
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
 * Full-stack test for {@code POST /api/admin/register-admin}: real Spring
 * Security filter chain, real method-security AOP (so
 * {@code @PreAuthorize("hasRole('ADMIN')")} is genuinely enforced), real
 * JPA repository, and an embedded H2 database. This is the only test class
 * that can actually prove the admin-only restriction works, since
 * {@code @PreAuthorize} isn't active on a bean built by hand in a plain
 * unit test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminRegisterAdminIntegrationTest {

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

    private String adminToken;
    private String userToken;

    @BeforeEach
    void seedExistingUsers() {
        User admin = userRepository.save(User.builder()
                .username("existing.admin")
                .email("existing.admin@example.com")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .role(Role.ADMIN)
                .build());
        User regularUser = userRepository.save(User.builder()
                .username("regular.user")
                .email("regular.user@example.com")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .role(Role.USER)
                .build());

        adminToken = jwtService.generateToken(UserDetailsFactory.from(admin));
        userToken = jwtService.generateToken(UserDetailsFactory.from(regularUser));
    }

    @Test
    void registerAdmin_returns201Created_andPersistsAdminRole_whenCallerIsAdmin() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "brand.new.admin", "brand.new.admin@example.com", "AnotherSecurePass1");

        mockMvc.perform(post("/api/admin/register-admin")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("brand.new.admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        User persisted = userRepository.findByEmail("brand.new.admin@example.com").orElseThrow();
        assertThat(persisted.getRole()).isEqualTo(Role.ADMIN);
        assertThat(passwordEncoder.matches("AnotherSecurePass1", persisted.getPassword())).isTrue();
    }

    @Test
    void registerAdmin_returns403Forbidden_whenCallerIsARegularUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "should.not.exist", "should.not.exist@example.com", "AnotherSecurePass1");

        mockMvc.perform(post("/api/admin/register-admin")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findByEmail("should.not.exist@example.com")).isEmpty();
    }

    @Test
    void registerAdmin_returns403Forbidden_whenCallerIsNotAuthenticated() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "anonymous.attempt", "anonymous.attempt@example.com", "AnotherSecurePass1");

        mockMvc.perform(post("/api/admin/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findByEmail("anonymous.attempt@example.com")).isEmpty();
    }
}
