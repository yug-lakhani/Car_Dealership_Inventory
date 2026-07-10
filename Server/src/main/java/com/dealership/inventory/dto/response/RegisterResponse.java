package com.dealership.inventory.dto.response;

import com.dealership.inventory.entity.User;

import java.time.Instant;

/**
 * What the client sees back after a successful registration. Deliberately
 * excludes the password hash.
 */
public record RegisterResponse(
        Long id,
        String username,
        String email,
        String role,
        Instant createdAt
) {

    public static RegisterResponse from(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt());
    }
}
