package com.dealership.inventory.config;

import com.dealership.inventory.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Wires JWT-based, stateless authentication into Spring Security.
 * <p>
 * Login/registration endpoints are public; everything else requires a
 * valid bearer token, checked by {@link JwtAuthFilter} before Spring
 * Security's own {@link UsernamePasswordAuthenticationFilter} runs.
 * <p>
 * Note: the {@code AuthenticationManager}/{@code UserDetailsService} used
 * to actually authenticate a username+password login (and to load users
 * for {@link JwtAuthFilter}) will be added once the {@code User} entity
 * and auth endpoints exist. Until then, Spring Boot's autoconfigured
 * in-memory {@code UserDetailsService} fallback satisfies the bean
 * dependency so the application context still starts.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {"/api/auth/**"};

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
