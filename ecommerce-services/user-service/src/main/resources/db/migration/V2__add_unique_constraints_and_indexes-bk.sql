-- V2__add_unique_constraints_and_indexes.sql

-- Add unique constraints (prevent race condition)
ALTER TABLE users
ADD CONSTRAINT uk_username UNIQUE (username);
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN email_verified_at TIMESTAMP NULL;

ALTER TABLE users
ADD CONSTRAINT uk_email UNIQUE (email);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_email_verification_token
    ON users(email_verification_token)
    WHERE email_verification_token IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_last_login ON users(last_login);

-- Composite index for common queries
CREATE INDEX IF NOT EXISTS idx_username_enabled ON users(username, enabled);
CREATE INDEX IF NOT EXISTS idx_email_enabled ON users(email, enabled);

-- Comment for documentation
COMMENT ON CONSTRAINT uk_username ON users IS 'Prevent duplicate usernames - race condition protection';
COMMENT ON CONSTRAINT uk_email ON users IS 'Prevent duplicate emails - race condition protection';

CREATE TABLE verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_verification_tokens_token ON verification_tokens(token);
CREATE INDEX idx_verification_tokens_user_id ON verification_tokens(user_id);
CREATE INDEX idx_verification_tokens_expires_at ON verification_tokens(expires_at);

-- Update existing users to be verified (migration)
UPDATE users SET email_verified = TRUE, enabled = TRUE WHERE enabled = TRUE;