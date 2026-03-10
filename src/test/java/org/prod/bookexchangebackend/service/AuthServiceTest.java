package org.prod.bookexchangebackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.prod.bookexchangebackend.dto.AuthenticatedUserResponse;
import org.prod.bookexchangebackend.dto.LoginUserRequest;
import org.prod.bookexchangebackend.dto.RegisterUserRequest;
import org.prod.bookexchangebackend.enums.UserRole;
import org.prod.bookexchangebackend.model.PlatformUser;
import org.prod.bookexchangebackend.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldSaveUserAndReturnJwtResponse() {
        RegisterUserRequest request = new RegisterUserRequest(
                "donor@example.com",
                "raw-password",
                "Donor",
                "User",
                "+79990001122",
                "Yekaterinburg",
                "bio",
                List.of(UserRole.DONOR));

        Instant expiresAt = Instant.parse("2026-03-10T10:00:00Z");

        when(userService.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(jwtService.generateToken(any(PlatformUser.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationInstant()).thenReturn(expiresAt);

        AuthenticatedUserResponse response = authService.register(request);

        ArgumentCaptor<PlatformUser> userCaptor = ArgumentCaptor.forClass(PlatformUser.class);
        verify(userService).save(userCaptor.capture());

        PlatformUser savedUser = userCaptor.getValue();
        assertThat(savedUser.email()).isEqualTo(request.email());
        assertThat(savedUser.passwordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.roles()).containsExactly(UserRole.DONOR);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.roles()).containsExactly(UserRole.DONOR);
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void register_shouldAssignReaderRoleWhenRolesAreMissing() {
        RegisterUserRequest request = new RegisterUserRequest(
                "reader@example.com", "raw-password", "Reader", "User", null, "Yekaterinburg", "bio", null);

        when(userService.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(jwtService.generateToken(any(PlatformUser.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationInstant()).thenReturn(Instant.parse("2026-03-10T10:00:00Z"));

        authService.register(request);

        ArgumentCaptor<PlatformUser> userCaptor = ArgumentCaptor.forClass(PlatformUser.class);
        verify(userService).save(userCaptor.capture());
        assertThat(userCaptor.getValue().roles()).containsExactly(UserRole.READER);
    }

    @Test
    void register_shouldRejectAdminRole() {
        RegisterUserRequest request = new RegisterUserRequest(
                "admin@example.com",
                "raw-password",
                "Admin",
                "User",
                null,
                "Yekaterinburg",
                "bio",
                List.of(UserRole.ADMIN));

        when(userService.existsByEmail(request.email())).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You cannot register an administrative user");

        verify(userService, never()).save(any());
    }

    @Test
    void login_shouldReturnJwtResponseWhenCredentialsAreValid() {
        PlatformUser user = new PlatformUser(
                UUID.randomUUID(),
                "reader@example.com",
                "encoded-password",
                "Reader",
                "User",
                null,
                "Yekaterinburg",
                "bio",
                List.of(UserRole.READER),
                true);

        Instant expiresAt = Instant.parse("2026-03-10T10:00:00Z");

        when(userService.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", user.passwordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationInstant()).thenReturn(expiresAt);

        AuthenticatedUserResponse response = authService.login(new LoginUserRequest(user.email(), "raw-password"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(user.id());
        assertThat(response.roles()).containsExactly(UserRole.READER);
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void login_shouldThrowWhenUserIsMissing() {
        when(userService.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginUserRequest("missing@example.com", "raw-password")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void login_shouldThrowWhenPasswordIsInvalid() {
        PlatformUser user = new PlatformUser(
                UUID.randomUUID(),
                "reader@example.com",
                "encoded-password",
                "Reader",
                "User",
                null,
                "Yekaterinburg",
                "bio",
                List.of(UserRole.READER),
                true);

        when(userService.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.passwordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginUserRequest(user.email(), "wrong-password")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}
