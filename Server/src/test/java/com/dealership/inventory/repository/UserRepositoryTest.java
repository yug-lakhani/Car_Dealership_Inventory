package com.dealership.inventory.repository;

import com.dealership.inventory.entity.Role;
import com.dealership.inventory.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

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
}
