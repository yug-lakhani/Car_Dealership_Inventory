package com.dealership.inventory.security;

import com.dealership.inventory.config.JwtProperties;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtService}. No Spring context is loaded - the
 * service is plain enough to be exercised with a hand-built
 * {@link JwtProperties} instance, keeping the tests fast.
 */
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-must-be-at-least-32-bytes-long";
    private static final long ONE_HOUR_MS = 1000L * 60 * 60;

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(SECRET, ONE_HOUR_MS));
        userDetails = new User(
                "john.doe",
                "irrelevant-password-hash",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_returnsUsernameEncodedInToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("john.doe");
    }

    @Test
    void isTokenValid_returnsTrue_forMatchingUserAndUnexpiredToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_whenUsernameDoesNotMatch() {
        String token = jwtService.generateToken(userDetails);
        UserDetails someoneElse = new User("jane.doe", "password", List.of());

        assertThat(jwtService.isTokenValid(token, someoneElse)).isFalse();
    }

    @Test
    void extractUsername_throws_forExpiredToken() {
        JwtService expiringImmediately = new JwtService(new JwtProperties(SECRET, -1000L));
        String token = expiringImmediately.generateToken(userDetails);

        assertThatThrownBy(() -> expiringImmediately.extractUsername(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void extractUsername_throws_forMalformedToken() {
        assertThatThrownBy(() -> jwtService.extractUsername("this-is-not-a-jwt"))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void extractUsername_throws_whenSignedWithADifferentSecret() {
        JwtService otherIssuer = new JwtService(
                new JwtProperties("a-completely-different-secret-key-of-32-bytes+", ONE_HOUR_MS));
        String token = otherIssuer.generateToken(userDetails);

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(SignatureException.class);
    }
}
