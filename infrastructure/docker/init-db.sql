-- ================================================
-- ECOMMERCE PLATFORM - DATABASE INIT (DOCKER SAFE)
-- ================================================

-- ================================================
-- ECOMMERCE SERVICES - SEPARATE DATABASES
-- ================================================

CREATE DATABASE ecommerce_auth;
CREATE DATABASE ecommerce_product;
CREATE DATABASE ecommerce_inventory;
CREATE DATABASE ecommerce_payment;
CREATE DATABASE ecommerce_order;
CREATE DATABASE ecommerce_cart;
CREATE DATABASE ecommerce_notification;

-- ================================================
-- PROMOX ENGINE - SHARED DATABASE
-- ================================================

CREATE DATABASE promox_db;

-- ================================================
-- PROMOX SCHEMAS
-- ================================================
\c promox_db

CREATE SCHEMA IF NOT EXISTS campaign;
CREATE SCHEMA IF NOT EXISTS promotion;
CREATE SCHEMA IF NOT EXISTS flashsale;
CREATE SCHEMA IF NOT EXISTS coupon;
CREATE SCHEMA IF NOT EXISTS reward;
CREATE SCHEMA IF NOT EXISTS analytics;

-- Permissions
GRANT ALL PRIVILEGES ON SCHEMA campaign   TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA promotion TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA flashsale TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA coupon    TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA reward    TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA analytics TO postgres;

-- Default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA campaign   GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA promotion GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA flashsale GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA coupon    GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA reward    GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA analytics GRANT ALL ON TABLES TO postgres;

-- Search path
ALTER DATABASE promox_db SET search_path TO
  public, campaign, promotion, flashsale, coupon, reward, analytics;

-- ================================================
-- GRANT DATABASE PERMISSIONS
-- ================================================
\c postgres

GRANT ALL PRIVILEGES ON DATABASE ecommerce_auth          TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_product       TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_inventory     TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_payment       TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_order         TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_cart          TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_notification  TO postgres;
GRANT ALL PRIVILEGES ON DATABASE promox_db               TO postgres;
