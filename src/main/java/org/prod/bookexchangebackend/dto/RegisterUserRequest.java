package org.prod.bookexchangebackend.dto;

import java.util.List;
import org.prod.bookexchangebackend.enums.UserRole;

public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber,
        String location,
        String bio,
        List<UserRole> roles) {
    public RegisterUserRequest {
        roles = roles != null ? roles : List.of(UserRole.READER);
    }
}
