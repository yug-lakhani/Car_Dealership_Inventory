package com.dealership.inventory.exception;

/**
 * Thrown when a registration attempt uses an email address that's already
 * associated with an existing account.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("An account with email '" + email + "' already exists");
    }
}
