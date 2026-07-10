package com.dealership.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe binding for the {@code app.jwt.*} properties declared in
 * {@code application.yml} (secret signing key and token time-to-live).
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
