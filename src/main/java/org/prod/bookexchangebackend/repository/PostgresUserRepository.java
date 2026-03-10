package org.prod.bookexchangebackend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.prod.bookexchangebackend.enums.UserRole;
import org.prod.bookexchangebackend.model.PlatformUser;
import org.prod.bookexchangebackend.repository.interfaces.UserRepository;
import org.prod.bookexchangebackend.repository.mapper.PlatformUserRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PostgresUserRepository implements UserRepository {

    private static final String FIND_USER_BY_EMAIL_SQL =
            """
            SELECT
                id,
                email,
                password_hash,
                first_name,
                last_name,
                phone_number,
                location,
                bio,
                active
            FROM platform_users
            WHERE email = ?
            """;

    private static final String FIND_USER_ROLES_SQL =
            """
            SELECT role
            FROM user_roles
            WHERE user_id = ?
            ORDER BY role
            """;

    private static final String EXISTS_BY_EMAIL_SQL =
            """
            SELECT EXISTS(
                SELECT 1
                FROM platform_users
                WHERE email = ?
            )
            """;

    private static final String INSERT_USER_SQL =
            """
            INSERT INTO platform_users (
                id,
                email,
                password_hash,
                first_name,
                last_name,
                phone_number,
                location,
                bio,
                active
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_ROLE_SQL =
            """
            INSERT INTO user_roles (user_id, role)
            VALUES (?, ?)
            """;

    private final PlatformUserRowMapper rowMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
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
                user.active());

        if (!user.roles().isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_ROLE_SQL, user.roles(), user.roles().size(), (ps, role) -> {
                ps.setObject(1, user.id());
                ps.setString(2, role.name());
            });
        }
    }

    @Override
    public Optional<PlatformUser> findByEmail(String email) {
        return jdbcTemplate.query(FIND_USER_BY_EMAIL_SQL, rowMapper, email).stream()
                .findFirst()
                .map(user -> withRoles(user, findRolesByUserId(user.id())));
    }

    @Override
    public boolean existsByEmail(String email) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(EXISTS_BY_EMAIL_SQL, Boolean.class, email));
    }

    private List<UserRole> findRolesByUserId(UUID userId) {
        return jdbcTemplate.query(FIND_USER_ROLES_SQL, (rs, rowNum) -> UserRole.valueOf(rs.getString("role")), userId);
    }

    private PlatformUser withRoles(PlatformUser user, List<UserRole> roles) {
        return new PlatformUser(
                user.id(),
                user.email(),
                user.passwordHash(),
                user.firstName(),
                user.lastName(),
                user.phoneNumber(),
                user.location(),
                user.bio(),
                roles,
                user.active());
    }
}
