package org.prod.bookexchangebackend.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.prod.bookexchangebackend.model.PlatformUser;
import org.prod.bookexchangebackend.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        PlatformUser user = userService
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user with email '" + email + "' not found"));

        return new AuthenticatedUserDetails(user.id(), user.email(), user.roles(), user.active());
    }
}
