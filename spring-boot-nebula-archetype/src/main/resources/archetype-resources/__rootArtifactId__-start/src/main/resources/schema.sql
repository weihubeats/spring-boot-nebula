CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    age         INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO sys_user (name, age) VALUES ('alice', 20), ('bob', 30);
