-- ============================================================
-- BOOKING SYSTEM - SEED DATA cho H2 in-memory (profile: mock)
-- Spring Boot tự động chạy file này khi khởi động
-- ============================================================

-- ============================================================
-- 1. PERMISSIONS
-- ============================================================
INSERT INTO permissions (name) VALUES ('USER_READ');
INSERT INTO permissions (name) VALUES ('USER_WRITE');
INSERT INTO permissions (name) VALUES ('USER_DELETE');
INSERT INTO permissions (name) VALUES ('ROLE_READ');
INSERT INTO permissions (name) VALUES ('ROLE_WRITE');
INSERT INTO permissions (name) VALUES ('BOOKING_READ');
INSERT INTO permissions (name) VALUES ('BOOKING_WRITE');
INSERT INTO permissions (name) VALUES ('BOOKING_DELETE');
INSERT INTO permissions (name) VALUES ('PAYMENT_READ');
INSERT INTO permissions (name) VALUES ('ADMIN_ALL');

-- ============================================================
-- 2. ROLES
-- ============================================================
INSERT INTO roles (role_name) VALUES ('ADMIN');
INSERT INTO roles (role_name) VALUES ('MANAGER');
INSERT INTO roles (role_name) VALUES ('USER');

-- ============================================================
-- 3. ROLE_PERMISSIONS
-- ADMIN (id=1): tất cả quyền
-- MANAGER (id=2): quản lý booking + đọc user
-- USER (id=3): chỉ đọc và tạo booking
-- ============================================================
INSERT INTO role_permissions (permission_id, role_id) VALUES (1,  1);
INSERT INTO role_permissions (permission_id, role_id) VALUES (2,  1);
INSERT INTO role_permissions (permission_id, role_id) VALUES (3,  1);
INSERT INTO role_permissions (permission_id, role_id) VALUES (4,  1);
INSERT INTO role_permissions (permission_id, role_id) VALUES (5,  1);
INSERT INTO role_permissions (permission_id, role_id) VALUES (6,  1);
INSERT INTO role_permissions (permission_id, role_id) VALUES (7,  1);
INSERT INTO role_permissions (permission_id, role_id) VALUES (8,  1);
INSERT INTO role_permissions (permission_id, role_id) VALUES (9,  1);
INSERT INTO role_permissions (permission_id, role_id) VALUES (10, 1);
INSERT INTO role_permissions (permission_id, role_id) VALUES (1,  2);
INSERT INTO role_permissions (permission_id, role_id) VALUES (6,  2);
INSERT INTO role_permissions (permission_id, role_id) VALUES (7,  2);
INSERT INTO role_permissions (permission_id, role_id) VALUES (8,  2);
INSERT INTO role_permissions (permission_id, role_id) VALUES (9,  2);
INSERT INTO role_permissions (permission_id, role_id) VALUES (6,  3);
INSERT INTO role_permissions (permission_id, role_id) VALUES (7,  3);

-- ============================================================
-- 4. USERS
-- Password BCrypt hash của "password123"
-- Dùng để test: name=admin / password=password123
-- ============================================================
INSERT INTO users (name, email, password, phone, created_at, updated_at) VALUES
    ('admin',        'admin@bookingsystem.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901234567', CURRENT_DATE, CURRENT_DATE);
INSERT INTO users (name, email, password, phone, created_at, updated_at) VALUES
    ('manager',      'manager@bookingsystem.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0902345678', CURRENT_DATE, CURRENT_DATE);
INSERT INTO users (name, email, password, phone, created_at, updated_at) VALUES
    ('nguyen_van_a', 'nguyenvana@gmail.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0903456789', CURRENT_DATE, CURRENT_DATE);
INSERT INTO users (name, email, password, phone, created_at, updated_at) VALUES
    ('tran_thi_b',   'tranthib@gmail.com',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0904567890', CURRENT_DATE, CURRENT_DATE);
INSERT INTO users (name, email, password, phone, created_at, updated_at) VALUES
    ('le_van_c',     'levanc@gmail.com',           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0905678901', CURRENT_DATE, CURRENT_DATE);

-- ============================================================
-- 5. USER_ROLES
-- ============================================================
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (3, 3);
INSERT INTO user_roles (user_id, role_id) VALUES (4, 3);
INSERT INTO user_roles (user_id, role_id) VALUES (5, 3);
