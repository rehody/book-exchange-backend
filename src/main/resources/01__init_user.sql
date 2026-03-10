CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS platform_users
(
    id                  UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    email               VARCHAR(320) UNIQUE NOT NULL,
    password_hash       TEXT                NOT NULL,
    first_name          VARCHAR(100),
    last_name           VARCHAR(100),
    phone_number        VARCHAR(32),
    location            VARCHAR(255),
    bio                 TEXT,
    roles               TEXT[]              NOT NULL DEFAULT ARRAY []::TEXT[],
    active              BOOLEAN             NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_platform_users_email ON platform_users (email);
CREATE INDEX IF NOT EXISTS idx_platform_users_active ON platform_users (active);