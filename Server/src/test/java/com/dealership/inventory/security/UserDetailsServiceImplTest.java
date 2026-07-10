package com.dealership.inventory.security;

import com.dealership.inventory.entity.Role;
import com.dealership.inventory.entity.User;
import com.dealership.inventory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserDetailsServiceImpl}, the bridge between our
 * {@link User} entity and Spring Security's {@link UserDetails} contract.
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl userDetailsService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsername_returnsUserDetailsWithRolePrefixedAuthority_whenUserExists() {
        User user = User.builder()
                .id(1L)
                .username("jane.admin")
                .email("jane.admin@example.com")
                .password("hashed-password")
                .role(Role.ADMIN)
                .build();
        when(userRepository.findByUsername("jane.admin")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("jane.admin");

        assertThat(userDetails.getUsername()).isEqualTo("jane.admin");
        assertThat(userDetails.getPassword()).isEqualTo("hashed-password");
        assertThat(userDetails.getAuthorities())
                .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nobody"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
