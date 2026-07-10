package com.dealership.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Declared separately from {@link SecurityConfig} to avoid a circular
 * dependency: {@code SecurityConfig} requires the {@code JwtAuthFilter}
 * bean, which requires a {@code UserDetailsService}. Until a real one is
 * added, Spring Boot's autoconfigured fallback {@code InMemoryUserDetailsManager}
 * satisfies that dependency - but it also looks up a {@link PasswordEncoder}
 * bean to hash its default user's password. If that bean lived on
 * {@code SecurityConfig}, resolving it would require instantiating
 * {@code SecurityConfig} while it's still being constructed.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
