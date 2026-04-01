-- 初始化用户 (uid: u001, u002)
INSERT INTO t_user (uid, username, age) VALUES ('u001', 'Alice', 25);
INSERT INTO t_user (uid, username, age) VALUES ('u002', 'Bob', 30);
INSERT INTO t_user (uid, username, age) VALUES ('u003', 'Charlie', 35); -- u003 无权限

-- 初始化订单 (creating_uid: u001)
INSERT INTO t_order (order_no, creating_uid, amount) VALUES ('ORD_001', 'u001', 100.00);
INSERT INTO t_order (order_no, creating_uid, amount) VALUES ('ORD_002', 'u003', 200.00);

-- 初始化商户 (merchant_code: m001)
INSERT INTO t_merchant (merchant_code, merchant_name) VALUES ('m001', 'Tesla Shop');
INSERT INTO t_merchant (merchant_code, merchant_name) VALUES ('m002', 'SpaceX Shop');

-- 初始化路由数据
-- 假设当前上下文是 region = 1
-- u001 在 region 1 (可见)
-- u002 在 region 2 (不可见)
INSERT INTO csa_user_route (uid, csa_region_id) VALUES ('u001', 1);
INSERT INTO csa_user_route (uid, csa_region_id) VALUES ('u002', 2);

-- 商户路由数据
-- m001 在 region 1 (可见)
INSERT INTO csa_merchant_route (m_id, csa_region_id) VALUES ('m001', 1);