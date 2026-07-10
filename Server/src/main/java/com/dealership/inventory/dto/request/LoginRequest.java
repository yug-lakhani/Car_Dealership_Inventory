package com.dealership.inventory.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload for {@code POST /api/auth/login}.
 * <p>
 * Deliberately has no {@code @Size} constraint on the password: login
 * shouldn't reveal anything about password policy, and an incorrect length
 * simply fails the credential check like any other wrong password.
 */
public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}
