package com.dealership.inventory.repository;

import com.dealership.inventory.entity.Role;
import com.dealership.inventory.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link UserRepository} slice test backed by a real (embedded) database,
 * verifying the custom query method Hibernate generates from its name.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByEmail_returnsTrue_whenAUserWithThatEmailWasPersisted() {
        User user = User.builder()
                .username("john.doe")
                .email("john.doe@example.com")
                .password("hashed-password")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("john.doe@example.com")).isTrue();
    }

    @Test
    void existsByEmail_returnsFalse_whenNoUserHasThatEmail() {
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void findByEmail_returnsUser_whenAUserWithThatEmailWasPersisted() {
        User user = User.builder()
                .username("jane.doe")
                .email("jane.doe@example.com")
                .password("hashed-password")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("jane.doe@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("jane.doe");
    }

    @Test
    void findByEmail_returnsEmpty_whenNoUserHasThatEmail() {
        assertThat(userRepository.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    void findByUsername_returnsUser_whenAUserWithThatUsernameWasPersisted() {
        User user = User.builder()
                .username("jane.admin")
                .email("jane.admin@example.com")
                .password("hashed-password")
                .role(Role.ADMIN)
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("jane.admin");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("jane.admin@example.com");
    }

    @Test
    void findByUsername_returnsEmpty_whenNoUserHasThatUsername() {
        assertThat(userRepository.findByUsername("nobody")).isEmpty();
    }
}
