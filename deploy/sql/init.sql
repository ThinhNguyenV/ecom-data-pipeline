-- ==========================================================
-- init.sql — PostgreSQL initialization for ecom-data-pipeline
-- Creates the raw layer schemas and tables that dbt reads from.
-- Run once before first dbt run:
--   psql -h localhost -U user -d ecom_dw -f deploy/sql/init.sql
-- ==========================================================

-- Schemas
CREATE SCHEMA IF NOT EXISTS ecom_raw;
CREATE SCHEMA IF NOT EXISTS ecom_staging;
CREATE SCHEMA IF NOT EXISTS ecom_marts;

-- -------------------------
-- Raw: customers
-- -------------------------
CREATE TABLE IF NOT EXISTS ecom_raw.customers (
    customer_id  VARCHAR(50)  PRIMARY KEY,
    first_name   VARCHAR(100),
    last_name    VARCHAR(100),
    email        VARCHAR(255) NOT NULL,
    country      VARCHAR(100),
    created_at   TIMESTAMP    DEFAULT NOW()
);

-- -------------------------
-- Raw: products
-- -------------------------
CREATE TABLE IF NOT EXISTS ecom_raw.products (
    product_id   VARCHAR(50)   PRIMARY KEY,
    sku          VARCHAR(100),
    product_name VARCHAR(255)  NOT NULL,
    category     VARCHAR(100),
    price        NUMERIC(12,2),
    updated_at   TIMESTAMP     DEFAULT NOW()
);

-- -------------------------
-- Raw: orders
-- -------------------------
CREATE TABLE IF NOT EXISTS ecom_raw.orders (
    order_id        VARCHAR(50)   PRIMARY KEY,
    customer_id     VARCHAR(50)   REFERENCES ecom_raw.customers(customer_id),
    status          VARCHAR(50)   DEFAULT 'pending',
    total_amount    NUMERIC(12,2) DEFAULT 0,
    payment_method  VARCHAR(50),
    raw_metadata    JSONB,
    created_at      TIMESTAMP     DEFAULT NOW()
);

-- -------------------------
-- Raw: order_items
-- -------------------------
CREATE TABLE IF NOT EXISTS ecom_raw.order_items (
    order_item_id VARCHAR(50)   PRIMARY KEY,
    order_id      VARCHAR(50)   REFERENCES ecom_raw.orders(order_id),
    product_id    VARCHAR(50)   REFERENCES ecom_raw.products(product_id),
    quantity      INTEGER       NOT NULL DEFAULT 1,
    unit_price    NUMERIC(12,2) NOT NULL
);

-- -------------------------
-- Raw: events
-- -------------------------
CREATE TABLE IF NOT EXISTS ecom_raw.events (
    event_id     VARCHAR(100) PRIMARY KEY,
    user_id      VARCHAR(50),
    anonymous_id VARCHAR(100),
    event_type   VARCHAR(100) NOT NULL,
    event_ts     TIMESTAMP    DEFAULT NOW(),
    session_id   VARCHAR(100),
    properties   JSONB
);

-- -------------------------
-- Seed data for development
-- -------------------------
INSERT INTO ecom_raw.customers (customer_id, first_name, last_name, email, country)
VALUES
    ('C001', 'Nguyen', 'Van A', 'vana@example.com', 'Vietnam'),
    ('C002', 'Tran',   'Thi B', 'thib@example.com', 'Vietnam'),
    ('C003', 'Le',     'Van C', 'vanc@example.com', 'Singapore')
ON CONFLICT DO NOTHING;

INSERT INTO ecom_raw.products (product_id, sku, product_name, category, price)
VALUES
    ('P001', 'SKU-001', 'Giày chạy bộ Nike Air Max',   'Footwear',    1500000),
    ('P002', 'SKU-002', 'Áo thun Adidas Essentials',   'Apparel',      450000),
    ('P003', 'SKU-003', 'Quần short tập gym Puma',     'Apparel',      380000),
    ('P004', 'SKU-004', 'Balo du lịch North Face',     'Accessories', 1200000),
    ('P005', 'SKU-005', 'Đồng hồ thông minh Apple Watch', 'Electronics', 9500000),
    ('P006', 'SKU-006', 'Tai nghe Sony WH-1000XM4',   'Electronics', 7800000),
    ('P007', 'SKU-007', 'Thảm tập Yoga Lululemon',     'Fitness',      850000),
    ('P008', 'SKU-008', 'Bình nước Hydro Flask',       'Accessories',  650000),
    ('P009', 'SKU-009', 'Vợt cầu lông Yonex Astrox',  'Sports',      1100000),
    ('P010', 'SKU-010', 'Giày đá bóng Adidas Predator','Footwear',   1350000)
ON CONFLICT DO NOTHING;

INSERT INTO ecom_raw.orders (order_id, customer_id, status, total_amount, payment_method, created_at)
VALUES
    ('ORD-001', 'C001', 'completed', 1950000, 'credit_card',  '2025-01-10 09:00:00'),
    ('ORD-002', 'C001', 'completed', 7800000, 'bank_transfer','2025-01-15 14:30:00'),
    ('ORD-003', 'C002', 'pending',    450000, 'cod',          '2025-02-01 10:00:00'),
    ('ORD-004', 'C003', 'completed', 9500000, 'credit_card',  '2025-02-10 16:00:00'),
    ('ORD-005', 'C002', 'cancelled',  380000, 'cod',          '2025-03-05 11:00:00')
ON CONFLICT DO NOTHING;

INSERT INTO ecom_raw.order_items (order_item_id, order_id, product_id, quantity, unit_price)
VALUES
    ('OI-001', 'ORD-001', 'P001', 1, 1500000),
    ('OI-002', 'ORD-001', 'P002', 1,  450000),
    ('OI-003', 'ORD-002', 'P006', 1, 7800000),
    ('OI-004', 'ORD-003', 'P002', 1,  450000),
    ('OI-005', 'ORD-004', 'P005', 1, 9500000),
    ('OI-006', 'ORD-005', 'P003', 1,  380000)
ON CONFLICT DO NOTHING;
