package org.prod.bookexchangebackend.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.prod.bookexchangebackend.dto.AuthenticatedUserResponse;
import org.prod.bookexchangebackend.dto.LoginUserRequest;
import org.prod.bookexchangebackend.dto.RegisterUserRequest;
import org.prod.bookexchangebackend.enums.UserRole;
import org.prod.bookexchangebackend.model.PlatformUser;
import org.prod.bookexchangebackend.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // в будущем следует перенести в отдельный маппер
    private static PlatformUser mapRegisterToUser(UUID id, String encodedPassword, RegisterUserRequest request) {
        return new PlatformUser(
                id,
                request.email(),
                encodedPassword,
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.location(),
                request.bio(),
                request.roles(),
                true);
    }

    private static void restrictAdminRegistration(RegisterUserRequest request) {
        if (request.roles().contains(UserRole.ADMIN)) {
            throw new IllegalArgumentException("You cannot register an administrative user");
        }
    }

    public AuthenticatedUserResponse register(RegisterUserRequest request) {
        ensureUserNotExists(request);
        restrictAdminRegistration(request);

        PlatformUser user = mapRegisterToUser(UUID.randomUUID(), passwordEncoder.encode(request.password()), request);

        userService.save(user);

        String token = jwtService.generateToken(user);
        Instant tokenExpiresAt = jwtService.getExpirationInstant();

        return new AuthenticatedUserResponse(token, user.id(), user.roles(), tokenExpiresAt);
    }

    private void ensureUserNotExists(RegisterUserRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }
    }

    public AuthenticatedUserResponse login(LoginUserRequest request) {
        PlatformUser user = userService
                .findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        Instant tokenExpiresAt = jwtService.getExpirationInstant();

        return new AuthenticatedUserResponse(token, user.id(), user.roles(), tokenExpiresAt);
    }
}
