package org.prod.bookexchangebackend.model;

import java.util.List;
import java.util.UUID;
import org.prod.bookexchangebackend.enums.UserRole;

public record PlatformUser(
        UUID id,
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        String phoneNumber,
        String location,
        String bio,
        List<UserRole> roles,
        boolean active) {
    public PlatformUser {
        roles = roles != null ? roles : List.of();
    }
}
