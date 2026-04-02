-- =====================================================
-- V1__create_users_tables.sql
-- HALAL-BITE User Service — Initial Schema
-- =====================================================
-- Flyway runs this script automatically on startup
-- if it hasn't been run before.
-- The version prefix V1__ ensures scripts run in order.
-- NEVER modify a migration after it has run —
-- create a new V2__ script for any changes.
-- =====================================================

-- Users table
CREATE TABLE users (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id  VARCHAR(255) NOT NULL UNIQUE,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index on keycloak_id — looked up on every authenticated request
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);

-- Index on email — checked during registration
CREATE INDEX idx_users_email ON users(email);

-- Index on is_active — most queries filter by active users
CREATE INDEX idx_users_is_active ON users(is_active);

-- User addresses table
CREATE TABLE user_addresses (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label          VARCHAR(50),
    street_address TEXT NOT NULL,
    city           VARCHAR(100) NOT NULL,
    state          VARCHAR(100),
    postal_code    VARCHAR(20) NOT NULL,
    country        VARCHAR(100) NOT NULL DEFAULT 'United States',
    is_default     BOOLEAN NOT NULL DEFAULT FALSE
);

-- Index on user_id — addresses are always fetched by user
CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);

-- =====================================================
-- WHY gen_random_uuid()?
-- PostgreSQL 13+ has this built-in function.
-- It generates a UUID at the database level.
-- Our Java code also generates UUIDs (@GeneratedValue UUID),
-- so the DEFAULT here is a safety fallback.
-- =====================================================
