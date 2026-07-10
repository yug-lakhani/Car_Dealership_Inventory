package com.dealership.inventory.service;

import com.dealership.inventory.dto.request.RegisterRequest;
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
}
