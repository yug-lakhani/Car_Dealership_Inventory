package com.dealership.inventory.entity;

/**
 * Authorization role assigned to a {@link User}. Every newly registered
 * user defaults to {@link #USER}; only existing admins can promote someone
 * to {@link #ADMIN} (not implemented yet).
 */
public enum Role {
    USER,
    ADMIN
}
