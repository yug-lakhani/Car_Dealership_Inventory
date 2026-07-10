package com.dealership.inventory.security;

import com.dealership.inventory.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * Builds the Spring Security {@link UserDetails} view of a {@link User}.
 * <p>
 * Centralised here so both {@link UserDetailsServiceImpl} (used by
 * {@link JwtAuthFilter} to authenticate incoming requests) and
 * {@code AuthServiceImpl} (used to issue a token right after login) derive
 * a user's {@link org.springframework.security.core.GrantedAuthority}
 * the same way: {@code "ROLE_" + role.name()}, which is exactly what
 * Spring Security's {@code hasRole(...)} expressions expect.
 */
public final class UserDetailsFactory {

    private UserDetailsFactory() {
    }

    public static UserDetails from(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}
