package org.prod.bookexchangebackend.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.prod.bookexchangebackend.model.PlatformUser;
import org.prod.bookexchangebackend.repository.interfaces.UserRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostgresUserRepository implements UserRepository {

    private static final String FIND_BY_EMAIL_SQL = "SELECT * FROM platform_users WHERE email = ?";
    private static final String EXISTS_BY_EMAIL_SQL = "SELECT EXISTS(SELECT 1 FROM platform_users WHERE email = ?)";
    private static final String INSERT_USER_SQL = "INSERT INTO platform_users "
            + "(id, email, password_hash, first_name, last_name, phone_number, location, bio, roles, active) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<PlatformUser> rowMapper = new BeanPropertyRowMapper<>(PlatformUser.class);

    @Override
    public void save(PlatformUser user) {
        jdbcTemplate.update(
                INSERT_USER_SQL,
                user.id(),
                user.email(),
                user.passwordHash(),
                user.firstName(),
                user.lastName(),
                user.phoneNumber(),
                user.location(),
                user.bio(),
                user.roles(),
                user.active());
    }

    @Override
    public Optional<PlatformUser> findByEmail(String email) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_BY_EMAIL_SQL, rowMapper, email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(EXISTS_BY_EMAIL_SQL, Boolean.class, email));
    }
}
