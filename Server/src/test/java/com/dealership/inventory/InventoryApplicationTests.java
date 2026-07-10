package com.dealership.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test verifying the full Spring context - including
 * {@code SecurityConfig} and the {@code JwtAuthFilter} bean it wires in -
 * starts up without errors.
 */
@SpringBootTest
@ActiveProfiles("test")
class InventoryApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: a failure here means the ApplicationContext
        // could not be assembled (missing beans, bad configuration, etc).
    }
}
