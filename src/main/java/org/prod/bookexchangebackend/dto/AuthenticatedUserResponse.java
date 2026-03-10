package org.prod.bookexchangebackend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.prod.bookexchangebackend.enums.UserRole;

public record AuthenticatedUserResponse(String token, UUID userId, List<UserRole> roles, Instant expiresAt) {}
