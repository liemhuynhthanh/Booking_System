-- ============================================================
-- BOOKING SYSTEM - SEED DATA
-- Dùng cho PostgreSQL (profile: dev / prod)
-- Chạy thủ công hoặc import vào pgAdmin / psql
-- ============================================================

-- Xóa dữ liệu cũ theo thứ tự (tránh lỗi FK)
DELETE FROM refresh_tokens;
DELETE FROM user_roles;
DELETE FROM role_permissions;
DELETE FROM users;
DELETE FROM roles;
DELETE FROM permissions;

-- Reset sequence
ALTER SEQUENCE permissions_id_seq RESTART WITH 1;
ALTER SEQUENCE roles_id_seq RESTART WITH 1;
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE refresh_tokens_id_seq RESTART WITH 1;

-- ============================================================
-- 1. PERMISSIONS
-- ============================================================
INSERT INTO permissions (name) VALUES
    ('USER_READ'),
    ('USER_WRITE'),
    ('USER_DELETE'),
    ('ROLE_READ'),
    ('ROLE_WRITE'),
    ('BOOKING_READ'),
    ('BOOKING_WRITE'),
    ('BOOKING_DELETE'),
    ('PAYMENT_READ'),
    ('ADMIN_ALL');

-- ============================================================
-- 2. ROLES
-- ============================================================
INSERT INTO roles (role_name) VALUES
    ('ADMIN'),
    ('MANAGER'),
    ('USER');

-- ============================================================
-- 3. ROLE_PERMISSIONS (liên kết Role <-> Permission)
-- ADMIN (id=1): có tất cả quyền
-- MANAGER (id=2): quản lý booking + đọc user
-- USER (id=3): chỉ đọc booking + tự quản lý
-- ============================================================
INSERT INTO role_permissions (permission_id, role_id) VALUES
    -- ADMIN: all permissions
    (1,  1), (2,  1), (3,  1), (4,  1), (5,  1),
    (6,  1), (7,  1), (8,  1), (9,  1), (10, 1),
    -- MANAGER: user_read, booking_all, payment_read
    (1,  2), (6,  2), (7,  2), (8,  2), (9,  2),
    -- USER: booking_read, booking_write
    (6,  3), (7,  3);

-- ============================================================
-- 4. USERS
-- Password đã được BCrypt hash (plain: "123")
-- Hash: $2a$10$slYQmyNdgTY18LGvgxPwHOQ9uMRcmVS0Ue3vp0UVcaFmNhsH.vHi
-- ============================================================
INSERT INTO users (name, email, password, phone, created_at, updated_at) VALUES
    ('admin',   'admin@bookingsystem.com',   '$2a$10$slYQmyNdgTY18LGvgxPwHOQ9uMRcmVS0Ue3vp0UVcaFmNhsH.vHi', '0901234567', CURRENT_DATE, CURRENT_DATE),
    ('manager', 'manager@bookingsystem.com', '$2a$10$slYQmyNdgTY18LGvgxPwHOQ9uMRcmVS0Ue3vp0UVcaFmNhsH.vHi', '0902345678', CURRENT_DATE, CURRENT_DATE),
    ('nguyen_van_a', 'nguyenvana@gmail.com', '$2a$10$slYQmyNdgTY18LGvgxPwHOQ9uMRcmVS0Ue3vp0UVcaFmNhsH.vHi', '0903456789', CURRENT_DATE, CURRENT_DATE),
    ('tran_thi_b',   'tranthib@gmail.com',   '$2a$10$slYQmyNdgTY18LGvgxPwHOQ9uMRcmVS0Ue3vp0UVcaFmNhsH.vHi', '0904567890', CURRENT_DATE, CURRENT_DATE),
    ('le_van_c',     'levanc@gmail.com',     '$2a$10$slYQmyNdgTY18LGvgxPwHOQ9uMRcmVS0Ue3vp0UVcaFmNhsH.vHi', '0905678901', CURRENT_DATE, CURRENT_DATE);

-- ============================================================
-- 5. USER_ROLES (liên kết User <-> Role)
-- ============================================================
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1),  -- admin      -> ADMIN
    (2, 2),  -- manager    -> MANAGER
    (3, 3),  -- nguyen_van_a -> USER
    (4, 3),  -- tran_thi_b   -> USER
    (5, 3);  -- le_van_c     -> USER

-- ============================================================
-- KIỂM TRA
-- ============================================================
-- SELECT u.name, u.email, r.role_name
-- FROM users u
-- JOIN user_roles ur ON u.id = ur.user_id
-- JOIN roles r ON r.id = ur.role_id
-- ORDER BY u.id;
