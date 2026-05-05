-- ============================================================
-- HALAL-BITE — PostgreSQL Database Initialisation Script
-- ============================================================
-- This script runs ONCE when the PostgreSQL container starts
-- for the very first time (via docker-entrypoint-initdb.d).
--
-- It creates a separate database for each microservice.
-- This is critical in a microservices architecture —
-- services MUST NOT share databases with each other.
-- ============================================================

-- Create all microservice databases
SELECT 'CREATE DATABASE auth_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auth_service_db')\gexec
SELECT 'CREATE DATABASE user_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'user_service_db')\gexec
SELECT 'CREATE DATABASE restaurant_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'restaurant_service_db')\gexec
SELECT 'CREATE DATABASE menu_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'menu_service_db')\gexec
SELECT 'CREATE DATABASE order_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'order_service_db')\gexec
SELECT 'CREATE DATABASE payment_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_service_db')\gexec
SELECT 'CREATE DATABASE notification_service_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_service_db')\gexec

-- Grant the app user full access to each database
GRANT ALL PRIVILEGES ON DATABASE user_service_db        TO halalbiteuser;
GRANT ALL PRIVILEGES ON DATABASE restaurant_service_db  TO halalbiteuser;
GRANT ALL PRIVILEGES ON DATABASE menu_service_db        TO halalbiteuser;
GRANT ALL PRIVILEGES ON DATABASE order_service_db       TO halalbiteuser;
GRANT ALL PRIVILEGES ON DATABASE payment_service_db     TO halalbiteuser;
GRANT ALL PRIVILEGES ON DATABASE notification_service_db TO halalbiteuser;
GRANT ALL PRIVILEGES ON DATABASE keycloak_db            TO halalbiteuser;

-- ============================================================
-- WHY SEPARATE DATABASES?
-- ============================================================
-- Each service owns its data. The order-service should NEVER
-- query the user-service's tables directly. It must call the
-- user-service's API instead. This enforces loose coupling
-- and lets each service evolve independently.
-- ============================================================
