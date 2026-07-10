package com.dealership.inventory.dto.response;

import com.dealership.inventory.entity.User;

/**
 * What the client sees back after a successful login: the JWT access token
 * plus basic profile info. Deliberately excludes the password hash.
 */
public record LoginResponse(
        String token,
        Long id,
        String username,
        String email,
        String role
) {

    public static LoginResponse of(String token, User user) {
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name());
    }
}
