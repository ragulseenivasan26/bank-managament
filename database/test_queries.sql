USE smart_bank;

-- Check all sample accounts
SELECT * FROM accounts;

-- Check transaction history
SELECT * FROM transactions ORDER BY transaction_time;

-- Test deposit
UPDATE accounts SET balance = balance + 1000 WHERE account_no = 100001;

-- Test balance
SELECT account_no, customer_name, balance
FROM accounts WHERE account_no = 100001;
