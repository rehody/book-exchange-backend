package org.prod.bookexchangebackend.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.prod.bookexchangebackend.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AuthenticatedUserDetails implements UserDetails {

    private final UUID userId;
    private final String username;
    private final List<SimpleGrantedAuthority> authorities;
    private final boolean active;

    public AuthenticatedUserDetails(UUID userId, String username, List<UserRole> roles, boolean active) {
        this.userId = userId;
        this.username = username;
        this.active = active;
        this.authorities = roles.stream()
                .map(UserRole::name)
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities != null ? authorities : List.of();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
