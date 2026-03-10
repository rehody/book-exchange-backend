package org.prod.bookexchangebackend.dto;

public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber,
        String location,
        String bio) {}
