
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ==========================================
-- 1. INSERT BẢNG ROLES
-- ==========================================
INSERT INTO roles (role_name) VALUES ('ADMIN');
INSERT INTO roles (role_name) VALUES ('USER');

-- ==========================================
-- 2. INSERT BẢNG USERS (Mật khẩu mặc định là 123456)
-- ==========================================
INSERT INTO users (name, email, password, phone, role_id, created_at, updated_at)
VALUES (
    'Liem Admin',
    'admin@gmail.com',
    crypt('123456', gen_salt('bf', 10)), -- Băm mật khẩu 123456
    '0901234567',
    (SELECT id FROM roles WHERE role_name = 'ADMIN' LIMIT 1),
    CURRENT_DATE,
    CURRENT_DATE
);

INSERT INTO users (name, email, password, phone, role_id, created_at, updated_at)
VALUES (
    'Liem User',
    'user@gmail.com',
    crypt('123456', gen_salt('bf', 10)),
    '0909888999',
    (SELECT id FROM roles WHERE role_name = 'USER' LIMIT 1),
    CURRENT_DATE,
    CURRENT_DATE
);

-- ==========================================
-- 3. INSERT BẢNG CONCERTS
-- ==========================================
INSERT INTO concerts (title, description, image_url, start_time, status, created_at)
VALUES (
    'BlackPink World Tour - Hanoi',
    'Đêm nhạc thế kỷ của BlackPink tại Sân vận động Mỹ Đình.',
    'https://example.com/blackpink.jpg',
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    'UPCOMING',
    CURRENT_TIMESTAMP
);

INSERT INTO concerts (title, description, image_url, start_time, status, created_at)
VALUES (
    'Taylor Swift The Eras Tour',
    'Hành trình âm nhạc qua các kỷ nguyên của Taylor Swift.',
    'https://example.com/taylor.jpg',
    CURRENT_TIMESTAMP + INTERVAL '60 days',
    'UPCOMING',
    CURRENT_TIMESTAMP
);

-- ==========================================
-- 4. INSERT BẢNG TICKET TYPES (Loại vé cho các Concerts)
-- ==========================================
INSERT INTO ticket_types (name, price, total_quantity, remaining_quantity, version, concert_id)
VALUES (
    'VVIP',
    10000000.00,
    1000,
    1000,
    0,
    (SELECT id FROM concerts WHERE title = 'BlackPink World Tour - Hanoi' LIMIT 1)
);

INSERT INTO ticket_types (name, price, total_quantity, remaining_quantity, concert_id)
VALUES (
    'STANDARD',
    3000000.00,
    5000,
    5000,
    (SELECT id FROM concerts WHERE title = 'BlackPink World Tour - Hanoi' LIMIT 1)
);

-- ==========================================
-- 5. INSERT BẢNG VOUCHERS
-- ==========================================
INSERT INTO vouchers (code, discount_type, discount_value, max_uses, used_count, expired_at)
VALUES (
    'WELCOME20',
    'PERCENTAGE',
    20.00,
    100,
    0,
    CURRENT_TIMESTAMP + INTERVAL '365 days'
);

INSERT INTO vouchers (code, discount_type, discount_value, max_uses, used_count, expired_at)
VALUES (
    'GIAM500K',
    'FIXED_AMOUNT',
    500000.00,
    50,
    0,
    CURRENT_TIMESTAMP + INTERVAL '10 days'
);
