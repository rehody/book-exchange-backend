package org.prod.bookexchangebackend.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.prod.bookexchangebackend.model.PlatformUser;
import org.prod.bookexchangebackend.repository.interfaces.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostgresUserRepository implements UserRepository {

    private static final String FIND_BY_EMAIL_SQL = "SELECT * FROM platform_users WHERE email = ?";
    private static final String EXISTS_BY_EMAIL_SQL = "SELECT EXISTS(SELECT 1 FROM platform_users WHERE email = ?)";
    private static final String INSERT_USER_SQL = "INSERT INTO platform_users "
            + "(id, email, passwordHash, firstName, lastName, phoneNumber, location, bio, roles) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(PlatformUser user) {
        jdbcTemplate.update(INSERT_USER_SQL);
    }

    @Override
    public Optional<PlatformUser> findByEmail(String email) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_BY_EMAIL_SQL, PlatformUser.class, email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(EXISTS_BY_EMAIL_SQL, Boolean.class, email));
    }
}
