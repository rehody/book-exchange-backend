package org.prod.bookexchangebackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.prod.bookexchangebackend.config.JacksonConfig;
import org.prod.bookexchangebackend.dto.AuthenticatedUserResponse;
import org.prod.bookexchangebackend.dto.LoginUserRequest;
import org.prod.bookexchangebackend.dto.RegisterUserRequest;
import org.prod.bookexchangebackend.enums.UserRole;
import org.prod.bookexchangebackend.security.AuthTokenFilter;
import org.prod.bookexchangebackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JacksonConfig.class)
class AuthControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    @Test
    void register_shouldReturnCreatedAndResponseBody() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                "reader@example.com",
                "raw-password",
                "Reader",
                "User",
                "+79990001122",
                "Yekaterinburg",
                "bio",
                List.of(UserRole.DONOR));
        AuthenticatedUserResponse response = new AuthenticatedUserResponse(
                "jwt-token",
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                List.of(UserRole.DONOR),
                Instant.parse("2026-03-10T10:00:00Z"));

        when(authService.register(any(RegisterUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.roles[0]").value("DONOR"));

        verify(authService).register(any(RegisterUserRequest.class));
    }

    @Test
    void login_shouldReturnOkAndResponseBody() throws Exception {
        LoginUserRequest request = new LoginUserRequest("reader@example.com", "raw-password");
        AuthenticatedUserResponse response = new AuthenticatedUserResponse(
                "jwt-token",
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                List.of(UserRole.DONOR),
                Instant.parse("2026-03-10T10:00:00Z"));

        when(authService.login(any(LoginUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value("22222222-2222-2222-2222-222222222222"))
                .andExpect(jsonPath("$.roles[0]").value("DONOR"));

        verify(authService).login(any(LoginUserRequest.class));
    }
}
