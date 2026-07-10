package com.dealership.inventory.security;

import com.dealership.inventory.entity.User;
import com.dealership.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads users from the database for Spring Security, replacing the
 * autoconfigured in-memory fallback. {@link JwtAuthFilter} calls this
 * (indirectly, via the {@link UserDetailsService} bean) to resolve the
 * username encoded in a JWT into the authorities used by
 * {@code @PreAuthorize} checks.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return UserDetailsFactory.from(user);
    }
}
