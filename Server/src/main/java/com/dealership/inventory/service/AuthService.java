package com.dealership.inventory.service;

import com.dealership.inventory.dto.request.LoginRequest;
import com.dealership.inventory.dto.request.RegisterRequest;
import com.dealership.inventory.dto.response.LoginResponse;
import com.dealership.inventory.dto.response.RegisterResponse;

/**
 * Business operations related to authentication/account creation.
 */
public interface AuthService {

    /**
     * Registers a new user with the {@code USER} role.
     *
     * @throws com.dealership.inventory.exception.EmailAlreadyExistsException
     *         if the email is already associated with an existing account.
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Authenticates a user by email/password and issues a JWT access token.
     *
     * @throws com.dealership.inventory.exception.InvalidCredentialsException
     *         if the email isn't registered, or the password doesn't match.
     */
    LoginResponse login(LoginRequest request);
}
