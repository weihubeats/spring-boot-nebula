DROP TABLE IF EXISTS user;
CREATE TABLE student
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(50),
    age   INT,
    email VARCHAR(50)
);