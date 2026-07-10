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
 * Unit tests for {@link AuthServiceImpl}. Collaborators ({@link UserRepository},
 * {@link PasswordEncoder}, {@link ModelMapper}) are mocked so the
 * registration business rules can be verified in isolation. ModelMapper's
 * *actual* record-to-entity mapping behaviour is exercised for real by
 * {@code AuthRegistrationIntegrationTest}, which boots the real bean.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    private AuthServiceImpl authService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, modelMapper);
    }

    @Test
    void register_savesNewUserWithHashedPasswordAndDefaultUserRole() {
        RegisterRequest request = new RegisterRequest("john.doe", "john.doe@example.com", "SecurePass123");

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(modelMapper.map(request, User.class)).thenReturn(
                User.builder().username("john.doe").email("john.doe@example.com").build());
        when(passwordEncoder.encode("SecurePass123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setId(1L);
            toSave.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return toSave;
        });

        RegisterResponse response = authService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("john.doe");
        assertThat(response.email()).isEqualTo("john.doe@example.com");
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUserCaptor.capture());
        User savedUser = savedUserCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("hashed-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void register_mapsRequestToUserViaModelMapper() {
        RegisterRequest request = new RegisterRequest("jane.doe", "jane.doe@example.com", "PlainTextPass1");
        User mappedUser = User.builder().username("jane.doe").email("jane.doe@example.com").build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(modelMapper.map(request, User.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode("PlainTextPass1")).thenReturn("bcrypt-hashed-value");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        verify(modelMapper).map(request, User.class);
        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue()).isSameAs(mappedUser);
    }

    @Test
    void register_hashesRawPasswordBeforePersisting() {
        RegisterRequest request = new RegisterRequest("jane.doe", "jane.doe@example.com", "PlainTextPass1");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(modelMapper.map(request, User.class)).thenReturn(new User());
        when(passwordEncoder.encode("PlainTextPass1")).thenReturn("bcrypt-hashed-value");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        verify(passwordEncoder).encode("PlainTextPass1");
        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue().getPassword())
                .isEqualTo("bcrypt-hashed-value")
                .isNotEqualTo("PlainTextPass1");
    }

    @Test
    void register_throwsEmailAlreadyExistsException_whenEmailIsAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("jane.doe", "jane.doe@example.com", "SecurePass123");
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, modelMapper);
    }
}
