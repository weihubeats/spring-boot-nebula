DROP TABLE IF EXISTS student;
CREATE TABLE student
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(50),
    age   INT,
    email VARCHAR(50),
    tags  VARCHAR(255) ARRAY,
    tags1  VARCHAR(255) ARRAY
);