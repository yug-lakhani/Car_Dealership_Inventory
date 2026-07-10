package com.dealership.inventory.service;

import com.dealership.inventory.dto.request.RegisterRequest;
import com.dealership.inventory.dto.response.RegisterResponse;

/**
 * Administrative user-management operations. Kept separate from
 * {@link AuthService} (which handles self-service register/login) since
 * this is a distinct, privileged responsibility: only an existing admin may
 * create another one.
 */
public interface AdminService {

    /**
     * Creates a new user with the {@code ADMIN} role. Restricted to callers
     * who are themselves already an admin.
     *
     * @throws com.dealership.inventory.exception.EmailAlreadyExistsException
     *         if the email is already associated with an existing account.
     */
    RegisterResponse createAdmin(RegisterRequest request);
}
