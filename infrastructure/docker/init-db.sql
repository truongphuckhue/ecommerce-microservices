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