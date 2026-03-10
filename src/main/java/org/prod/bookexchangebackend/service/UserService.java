package org.prod.bookexchangebackend.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.prod.bookexchangebackend.model.PlatformUser;
import org.prod.bookexchangebackend.repository.interfaces.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<PlatformUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void save(PlatformUser user) {
        userRepository.save(user);
    }
}
