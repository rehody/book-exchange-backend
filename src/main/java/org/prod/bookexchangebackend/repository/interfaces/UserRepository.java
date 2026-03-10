package org.prod.bookexchangebackend.repository.interfaces;

import java.util.Optional;
import org.prod.bookexchangebackend.model.PlatformUser;

public interface UserRepository {
    void save(PlatformUser user);

    Optional<PlatformUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
