package com.dealership.inventory.security;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JwtAuthFilter}. Collaborators are mocked so the
 * filter's control flow can be verified in isolation from the rest of the
 * security infrastructure.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(jwtService, userDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_continuesChainWithoutAuthentication_whenNoAuthorizationHeader() throws Exception {
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_continuesChainWithoutAuthentication_whenHeaderIsNotBearerScheme() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_authenticatesUser_whenTokenIsValid() throws Exception {
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        UserDetails userDetails = new User(
                "john.doe", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("john.doe");
        when(userDetailsService.loadUserByUsername("john.doe")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.jwt.token", userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        assertThat(authentication.getAuthorities())
                .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_doesNotAuthenticate_whenTokenIsInvalid() throws Exception {
        request.addHeader("Authorization", "Bearer invalid.jwt.token");
        UserDetails userDetails = new User("john.doe", "password", List.of());

        when(jwtService.extractUsername("invalid.jwt.token")).thenReturn("john.doe");
        when(userDetailsService.loadUserByUsername("john.doe")).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid.jwt.token", userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_doesNotAuthenticateAndStillContinuesChain_whenTokenParsingThrows() throws Exception {
        request.addHeader("Authorization", "Bearer malformed.token");

        when(jwtService.extractUsername("malformed.token")).thenThrow(new MalformedJwtException("bad token"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_doesNotReAuthenticate_whenSecurityContextAlreadyHasAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        Authentication existingAuth = new UsernamePasswordAuthenticationToken("existing-user", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        when(jwtService.extractUsername(any())).thenReturn("john.doe");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }
}
