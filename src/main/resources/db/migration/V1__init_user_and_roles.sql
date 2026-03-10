CREATE TABLE IF NOT EXISTS platform_users
(
    id            UUID PRIMARY KEY,
    email         VARCHAR(320) UNIQUE NOT NULL,
    password_hash TEXT                NOT NULL,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    phone_number  VARCHAR(32),
    location      VARCHAR(255),
    bio           TEXT,
    active        BOOLEAN             NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id UUID        NOT NULL REFERENCES platform_users (id) ON DELETE CASCADE,
    role    VARCHAR(16) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT chk_user_roles_role CHECK (role IN ('READER', 'DONOR', 'ADMIN'))
);

CREATE INDEX IF NOT EXISTS idx_platform_users_email ON platform_users (email);
CREATE INDEX IF NOT EXISTS idx_platform_users_active ON platform_users (active);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles (role);
