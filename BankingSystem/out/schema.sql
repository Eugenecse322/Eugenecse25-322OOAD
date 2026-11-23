-- BankingSystem schema (H2, in-memory)
-- One customer holds 1..* accounts and every account keeps its own transaction history.

CREATE TABLE IF NOT EXISTS customers (
    customer_id   INT AUTO_INCREMENT PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    surname       VARCHAR(100) NOT NULL,
    address       VARCHAR(255),
    pin           VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS accounts (
    account_id      INT AUTO_INCREMENT PRIMARY KEY,
    account_number  VARCHAR(20)  NOT NULL UNIQUE,
    account_type    VARCHAR(20)  NOT NULL
                    CHECK (account_type IN ('Savings', 'Investment', 'Cheque')),
    branch          VARCHAR(100) NOT NULL,
    balance         DECIMAL(15,2) NOT NULL DEFAULT 0,
    customer_id     INT NOT NULL
                    REFERENCES customers (customer_id),
    -- Cheque accounts only (rule R5): employment is mandatory for that type.
    company_name    VARCHAR(150),
    company_address VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id  INT AUTO_INCREMENT PRIMARY KEY,
    account_number  VARCHAR(20) NOT NULL
                    REFERENCES accounts (account_number),
    txn_type        VARCHAR(20) NOT NULL
                    CHECK (txn_type IN ('DEPOSIT', 'WITHDRAWAL')),
    amount          DECIMAL(15,2) NOT NULL,
    balance_after   DECIMAL(15,2) NOT NULL,
    description     VARCHAR(255),
    txn_datetime    TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_account
    ON transactions (account_number);
