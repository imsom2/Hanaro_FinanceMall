-- ── 1. USER ──────────────────────────────────────────────────────
INSERT INTO user (email, passwd, name, role)
VALUES
    ('admin@hanaro.com', 'admin1234!', '관리자', 'ROLE_ADMIN'),
    ('user1@hanaro.com', 'user1234!',  '홍길동', 'ROLE_USER'),
    ('user2@hanaro.com', 'user1234!',  '김하나', 'ROLE_USER');

-- ── 2. PRODUCT ───────────────────────────────────────────────────
INSERT INTO product (productName, productType, min, max, period, maturityYield, cancelYield, description, isDeleted)
VALUES
    ('하나 안심 정기예금',    '예금', 100000, 100000000, 12, 3.50, 1.00, '1년 만기 정기예금 상품입니다. 안정적인 수익을 원하시는 고객님께 추천드립니다.', false),
    ('하나 목돈 마련 적금',   '적금',  10000,   1000000, 12, 4.20, 1.50, '매월 자유롭게 납입하는 적금 상품입니다. 목돈 마련에 최적화된 상품입니다.',   false),
    ('(종료) 하나 특별 적금', '적금',  50000,    500000,  6, 5.00, 2.00, '종료된 상품입니다.',true);

-- ── 3. PRODUCT_IMAGE ─────────────────────────────────────────────
INSERT INTO ProductImage (product, orgName, saveName, saveDir, sortOrder)
VALUES
    (1, 'deposit_main.jpg',   '20260314_deposit_main.jpg',   '2026/03/14', 1),
    (1, 'deposit_detail.jpg', '20260314_deposit_detail.jpg', '2026/03/14', 2),
    (2, 'savings_main.jpg',   '20260314_savings_main.jpg',   '2026/03/14', 1),
    (2, 'savings_detail.jpg', '20260314_savings_detail.jpg', '2026/03/14', 2);

-- ── 4. ACCOUNT ───────────────────────────────────────────────────
INSERT INTO account (user, accountNum, accountType, balance)
VALUES
    (2, '11012345678', '기본계좌', 5000000),
    (2, '11098765432', '상품계좌', 0),
    (3, '11011122233', '기본계좌', 3000000),
    (3, '11044455566', '상품계좌', 0);

-- ── 5. SUBSCRIPTION ──────────────────────────────────────────────
INSERT INTO subscription (product, account, user, paymentAmount, paymentCycle, joinDate, endDate, status, maturedAt, cancelledAt)
VALUES
    (1, 2, 2, 5000000, '월', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), INTERVAL 12 MONTH), 'ACTIVE',    NULL, NULL),
    (2, 4, 3,  300000, '월', DATE_SUB(CURDATE(), INTERVAL 1 MONTH), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), INTERVAL 12 MONTH), 'ACTIVE',    NULL, NULL),
    (3, 2, 2,  100000, '월', DATE_SUB(CURDATE(), INTERVAL 8 MONTH), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 8 MONTH), INTERVAL 6 MONTH),  'CANCELLED', NULL, DATE_SUB(NOW(), INTERVAL 5 MONTH));

-- ── 6. INTEREST ──────────────────────────────────────────────────
INSERT INTO interest (subscription, amount, calcDate, appliedRate, elapsedDays)
VALUES
    (1, 12671.23, DATE_SUB(CURDATE(), INTERVAL 2 MONTH), 3.50, 30),
    (1, 25342.47, DATE_SUB(CURDATE(), INTERVAL 1 MONTH), 3.50, 60),
    (1, 38013.70, CURDATE(),                             3.50, 90),
    (2,  1726.03, CURDATE(),                             4.20, 30);
