package org.prod.bookexchangebackend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.prod.bookexchangebackend.config.AbstractIntegrationDatabaseTest;
import org.prod.bookexchangebackend.enums.UserRole;
import org.prod.bookexchangebackend.model.PlatformUser;
import org.prod.bookexchangebackend.repository.mapper.PlatformUserRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import({PostgresUserRepository.class, PlatformUserRowMapper.class})
class PostgresUserRepositoryIntegrationTest extends AbstractIntegrationDatabaseTest {

    @Autowired
    private PostgresUserRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SuppressWarnings("SqlWithoutWhere")
    @BeforeEach
    void setUp() {
        jdbcTemplate.execute(
                """
                CREATE TABLE IF NOT EXISTS platform_users (
                    id UUID PRIMARY KEY,
                    email VARCHAR(320) UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    first_name VARCHAR(100),
                    last_name VARCHAR(100),
                    phone_number VARCHAR(32),
                    location VARCHAR(255),
                    bio TEXT,
                    active BOOLEAN NOT NULL DEFAULT TRUE
                )
                """);
        jdbcTemplate.execute(
                """
                CREATE TABLE IF NOT EXISTS user_roles (
                    user_id UUID NOT NULL REFERENCES platform_users (id) ON DELETE CASCADE,
                    role VARCHAR(16) NOT NULL,
                    PRIMARY KEY (user_id, role),
                    CONSTRAINT chk_user_roles_role CHECK (role IN ('READER', 'DONOR', 'ADMIN'))
                )
                """);
        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("DELETE FROM platform_users");
    }

    @Test
    void save_shouldPersistUserAndRoles() {
        PlatformUser user = new PlatformUser(
                UUID.randomUUID(),
                "reader@example.com",
                "encoded-password",
                "Reader",
                "User",
                "+79990001122",
                "Yekaterinburg",
                "bio",
                List.of(UserRole.READER, UserRole.DONOR),
                true);

        repository.save(user);

        Integer usersCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM platform_users WHERE id = ?", Integer.class, user.id());
        List<String> roles = jdbcTemplate.queryForList(
                "SELECT role FROM user_roles WHERE user_id = ? ORDER BY role", String.class, user.id());

        assertThat(usersCount).isEqualTo(1);
        assertThat(roles).containsExactly("DONOR", "READER");
    }

    @Test
    void findByEmail_shouldReturnUserWithRoles() {
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
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
                """,
                userId,
                "reader@example.com",
                "encoded-password",
                "Reader",
                "User",
                "+79990001122",
                "Yekaterinburg",
                "bio",
                true);
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role) VALUES (?, ?)", userId, "READER");
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role) VALUES (?, ?)", userId, "DONOR");

        Optional<PlatformUser> result = repository.findByEmail("reader@example.com");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().id()).isEqualTo(userId);
        assertThat(result.orElseThrow().email()).isEqualTo("reader@example.com");
        assertThat(result.orElseThrow().passwordHash()).isEqualTo("encoded-password");
        assertThat(result.orElseThrow().roles()).containsExactly(UserRole.DONOR, UserRole.READER);
        assertThat(result.orElseThrow().active()).isTrue();
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenUserDoesNotExist() {
        Optional<PlatformUser> result = repository.findByEmail("missing@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_shouldReturnFalseWhenUserDoesNotExist() {
        boolean exists = repository.existsByEmail("missing@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_shouldReturnTrueWhenUserExists() {
        String email = "reader@example.com";
        jdbcTemplate.update(
                "INSERT INTO platform_users (id, email, password_hash, active) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(),
                email,
                "encoded-password",
                true);

        boolean exists = repository.existsByEmail(email);

        assertThat(exists).isTrue();
    }
}
