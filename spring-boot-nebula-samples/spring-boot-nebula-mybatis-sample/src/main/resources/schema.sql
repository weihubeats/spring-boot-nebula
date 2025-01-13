DROP TABLE IF EXISTS student;
CREATE TABLE student
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(50),
    age   INT,
    email VARCHAR(50)
);

insert into student(name, age, email)
values ('xiaozou', 18, 'xiaozou@163.com');