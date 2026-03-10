package org.prod.bookexchangebackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String INVALID_JWT_TOKEN_MESSAGE = "Invalid JWT token";
    private static final int BEARER_TOKEN_START_INDEX = BEARER_PREFIX.length();
    private static final List<String> PUBLIC_PATH_PREFIXES =
            List.of("/api/auth", "/api/register", "/actuator/health", "/actuator/info", "/swagger-ui", "/v3/api-docs");

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(requestUri::startsWith);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractBearerToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = extractValidatedEmail(token);
        authenticateIfNeeded(email);

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorizationHeader.substring(BEARER_TOKEN_START_INDEX);
    }

    private String extractValidatedEmail(String token) {
        try {
            return jwtService.extractEmail(token);
        } catch (Exception exception) {
            throw invalidJwtToken(exception);
        }
    }

    private BadCredentialsException invalidJwtToken(Exception exception) {
        return new BadCredentialsException(INVALID_JWT_TOKEN_MESSAGE, exception);
    }

    private void authenticateIfNeeded(String email) {
        if (email == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        validateUserStatus(userDetails);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void validateUserStatus(UserDetails userDetails) {
        if (!userDetails.isEnabled()) {
            throw new DisabledException("User is inactive");
        }
    }
}
