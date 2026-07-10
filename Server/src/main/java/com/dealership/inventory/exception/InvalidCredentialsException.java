package com.dealership.inventory.exception;

/**
 * Thrown when a login attempt fails - either the email isn't registered or
 * the password doesn't match. Deliberately carries the same generic
 * message for both cases: revealing *which* part of the credentials was
 * wrong would let an attacker enumerate registered email addresses.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
