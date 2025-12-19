-- ================================================
-- ECOMMERCE PLATFORM - OPTIMAL DATABASE DESIGN
-- ================================================
-- Design philosophy:
-- - Ecommerce Services: Separate databases (different bounded contexts)
-- - Promox Engine: Shared database (same bounded context)
-- ================================================

-- ================================================
-- ECOMMERCE SERVICES - SEPARATE DATABASES
-- ================================================
-- Each service is a different bounded context
-- Should be able to scale and deploy independently

CREATE DATABASE ecommerce_auth;
CREATE DATABASE ecommerce_product;
CREATE DATABASE ecommerce_inventory;
CREATE DATABASE ecommerce_payment;
CREATE DATABASE ecommerce_order;
CREATE DATABASE ecommerce_cart;
CREATE DATABASE ecommerce_notification;

-- ================================================
-- PROMOX ENGINE - SHARED DATABASE WITH SCHEMAS
-- ================================================
-- All promox services belong to same bounded context
-- Need to JOIN data frequently for analytics
-- Need ACID transactions for campaign creation

CREATE DATABASE promox_db;

-- Connect to promox_db to create schemas
\c promox_db

CREATE SCHEMA IF NOT EXISTS campaign;
CREATE SCHEMA IF NOT EXISTS promotion;
CREATE SCHEMA IF NOT EXISTS flashsale;
CREATE SCHEMA IF NOT EXISTS coupon;
CREATE SCHEMA IF NOT EXISTS reward;
CREATE SCHEMA IF NOT EXISTS analytics;

-- Grant permissions on schemas
GRANT ALL PRIVILEGES ON SCHEMA campaign TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA promotion TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA flashsale TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA coupon TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA reward TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA analytics TO postgres;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA campaign GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA promotion GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA flashsale GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA coupon GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA reward GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA analytics GRANT ALL ON TABLES TO postgres;

-- Set search path for convenience
ALTER DATABASE promox_db SET search_path TO
  public, campaign, promotion, flashsale, coupon, reward, analytics;

-- ================================================
-- GRANT PERMISSIONS TO POSTGRES USER
-- ================================================
\c postgres

GRANT ALL PRIVILEGES ON DATABASE ecommerce_auth TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_product TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_inventory TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_payment TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_order TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_cart TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_notification TO postgres;
GRANT ALL PRIVILEGES ON DATABASE promox_db TO postgres;

-- ================================================
-- DATABASE MAPPING
-- ================================================
-- Ecommerce Services (Separate databases):
-- - user-service        → ecommerce_auth
-- - product-service     → ecommerce_product
-- - inventory-service   → ecommerce_inventory
-- - payment-service     → ecommerce_payment
-- - order-service       → ecommerce_order
-- - cart-service        → ecommerce_cart
-- - notification-service → ecommerce_notification

-- Promox Engine (Shared database with schemas):
-- - campaign-service    → promox_db / campaign schema
-- - promotion-service   → promox_db / promotion schema
-- - flashsale-service   → promox_db / flashsale schema
-- - coupon-service      → promox_db / coupon schema
-- - reward-service      → promox_db / reward schema
-- - analytics-service   → promox_db / analytics schema

-- ================================================
-- NOTES
-- ================================================
-- Why separate databases for Ecommerce Services?
-- ✅ Different bounded contexts
-- ✅ Can scale independently
-- ✅ Deploy independently
-- ✅ True microservices isolation

-- Why shared database for Promox Engine?
-- ✅ Same bounded context (marketing/promotions)
-- ✅ Need to JOIN data (campaign → promotion → coupon)
-- ✅ Analytics need aggregate all data
-- ✅ ACID transactions (create campaign + promotions atomically)
-- ✅ Better performance (no network calls)
-- ✅ Simpler architecture for cohesive domain

-- Tables will be created automatically by JPA/Hibernate when services start

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