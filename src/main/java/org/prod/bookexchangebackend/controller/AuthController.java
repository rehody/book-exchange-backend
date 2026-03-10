package org.prod.bookexchangebackend.controller;

import lombok.RequiredArgsConstructor;
import org.prod.bookexchangebackend.dto.AuthenticatedUserResponse;
import org.prod.bookexchangebackend.dto.LoginUserRequest;
import org.prod.bookexchangebackend.dto.RegisterUserRequest;
import org.prod.bookexchangebackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticatedUserResponse register(@RequestBody RegisterUserRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthenticatedUserResponse login(@RequestBody LoginUserRequest request) {
        return authService.login(request);
    }
}
