package com.dealership.inventory.service.impl;

import com.dealership.inventory.dto.request.RegisterRequest;
import com.dealership.inventory.dto.response.RegisterResponse;
import com.dealership.inventory.entity.Role;
import com.dealership.inventory.entity.User;
import com.dealership.inventory.exception.EmailAlreadyExistsException;
import com.dealership.inventory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdminServiceImpl}. {@code @PreAuthorize} on
 * {@code createAdmin} is enforced by Spring's method-security AOP proxy,
 * which isn't active when constructing the bean directly in a unit test -
 * that restriction is verified separately by
 * {@code AdminRegisterAdminIntegrationTest}.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    private AdminServiceImpl adminService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(userRepository, passwordEncoder, modelMapper);
    }

    @Test
    void createAdmin_savesNewUserWithHashedPasswordAndAdminRole() {
        RegisterRequest request = new RegisterRequest("new.admin", "new.admin@example.com", "SecurePass123");

        when(userRepository.existsByEmail("new.admin@example.com")).thenReturn(false);
        when(modelMapper.map(request, User.class)).thenReturn(
                User.builder().username("new.admin").email("new.admin@example.com").build());
        when(passwordEncoder.encode("SecurePass123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setId(7L);
            toSave.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return toSave;
        });

        RegisterResponse response = adminService.createAdmin(request);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.username()).isEqualTo("new.admin");
        assertThat(response.email()).isEqualTo("new.admin@example.com");
        assertThat(response.role()).isEqualTo("ADMIN");

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUserCaptor.capture());
        User savedUser = savedUserCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("hashed-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void createAdmin_throwsEmailAlreadyExistsException_whenEmailIsAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("new.admin", "taken@example.com", "SecurePass123");
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createAdmin(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, modelMapper);
    }
}
