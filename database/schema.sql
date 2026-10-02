CREATE DATABASE IF NOT EXISTS smart_bank;
USE smart_bank;

CREATE TABLE IF NOT EXISTS accounts (
    account_no INT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    balance DECIMAL(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_no INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_no) REFERENCES accounts(account_no)
);

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL
);

INSERT INTO users(username,password) VALUES
('admin','admin123')
ON DUPLICATE KEY UPDATE password='admin123';

INSERT INTO accounts(account_no,customer_name,account_type,phone,balance) VALUES
(100001,'Ragul S','Savings','9876543210',25000.00),
(100002,'Arun Kumar','Current','9876501234',50000.00),
(100003,'Priya S','Savings','9123456780',15000.00),
(100004,'Karthik M','Current','9000011111',32500.00)
ON DUPLICATE KEY UPDATE customer_name=VALUES(customer_name),
account_type=VALUES(account_type), phone=VALUES(phone), balance=VALUES(balance);

INSERT INTO transactions(account_no,transaction_type,amount) VALUES
(100001,'DEPOSIT',10000.00),
(100001,'WITHDRAW',2500.00),
(100002,'DEPOSIT',50000.00),
(100003,'DEPOSIT',15000.00),
(100004,'DEPOSIT',32500.00);
