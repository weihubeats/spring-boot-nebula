-- 1. 普通用户表 (默认场景)
CREATE TABLE IF NOT EXISTS t_user (
                                      id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                      uid VARCHAR(32),
    username VARCHAR(50),
    age INT
    );

-- 2. 订单表 (测试自定义 mainColumn = creating_uid)
CREATE TABLE IF NOT EXISTS t_order (
                                       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                       order_no VARCHAR(50),
    creating_uid VARCHAR(32), -- 关联字段不是 uid
    amount DECIMAL(10, 2)
    );

-- 3. 商户表 (测试自定义 routeTable 和 routeColumn)
CREATE TABLE IF NOT EXISTS t_merchant (
                                          id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                          merchant_code VARCHAR(32), -- 关联字段
    merchant_name VARCHAR(50)
    );

-- 4. 用户权限路由表 (默认路由表)
CREATE TABLE IF NOT EXISTS csa_user_route (
                                              id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                              uid VARCHAR(32),
    csa_region_id INT
    );

-- 5. 商户权限路由表 (自定义路由表)
CREATE TABLE IF NOT EXISTS csa_merchant_route (
                                                  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                                  m_id VARCHAR(32), -- 路由表关联字段不是 uid
    csa_region_id INT
    );

