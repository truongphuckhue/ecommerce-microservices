-- ================================================
-- USER SERVICE - INITIAL SCHEMA
-- ================================================

-- ======================
-- USERS TABLE
-- ======================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified_at TIMESTAMP NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- ======================
-- CONSTRAINTS
-- ======================
ALTER TABLE users
    ADD CONSTRAINT uk_username UNIQUE (username);

ALTER TABLE users
    ADD CONSTRAINT uk_email UNIQUE (email);

-- ======================
-- INDEXES
-- ======================
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_created_at ON users(created_at);
CREATE INDEX idx_last_login ON users(last_login);

CREATE INDEX idx_username_enabled ON users(username, enabled);
CREATE INDEX idx_email_enabled ON users(email, enabled);

-- ======================
-- VERIFICATION TOKENS
-- ======================
CREATE TABLE verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_verification_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_verification_tokens_token
    ON verification_tokens(token);

CREATE INDEX idx_verification_tokens_user_id
    ON verification_tokens(user_id);

CREATE INDEX idx_verification_tokens_expires_at
    ON verification_tokens(expires_at);

-- ======================
-- COMMENTS
-- ======================
COMMENT ON CONSTRAINT uk_username ON users IS
    'Prevent duplicate usernames - race condition protection';

COMMENT ON CONSTRAINT uk_email ON users IS
    'Prevent duplicate emails - race condition protection';

-- ======================
-- DATA MIGRATION
-- ======================
UPDATE users
SET email_verified = TRUE,
    enabled = TRUE
WHERE enabled = TRUE;
