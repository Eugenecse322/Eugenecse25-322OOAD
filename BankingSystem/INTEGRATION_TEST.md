# Integration Test Report — Pula Bank

**Module:** CSE202 OOAD with Java — Part B, Week Nov 03–07 (Module Integration & Testing)
**Date:** 23 November 2026
**Tester:** [Your name / student number]

## 1. Objective

Verify that all modules of the Banking System work together as one application:
the JavaFX boundary (GUI) classes, the controller classes, the core domain model,
and the JDBC data access (DAO) layer backed by the H2 database.

## 2. What was tested

One complete user flow, exercised end-to-end by `src/IntegrationTest.java`:

| Step | Layer exercised | Action simulated |
|---|---|---|
| 1 | JDBC / DAO | `session.loadFromDatabase()` — reads customers, accounts and transactions through `CustomerDAO`, `AccountDAO`, `TransactionDAO` |
| 2 | Controller logic | Register a new customer (`RegisterController` flow) and persist it |
| 3 | Controller logic | "Log in" with name + PIN, verified through the SHA-256 hash (`LoginController` flow) |
| 4 | Controller logic | Open one of each account type (`OpenAccountController` flow) |
| 5 | Domain model | Deposit, withdraw and pay interest on the accounts (`AccountController` flow → `Account.deposit/debit/payInterest`) |
| 6 | JDBC / DAO | `session.persist()` writes everything back through the DAOs |
| 7 | JDBC / DAO | A **fresh** `BankSession` reloads the database — proving data really came from H2, not memory |

## 3. How to run

```
javac -d out src\*.java
copy src\schema.sql out\
java -cp "out;lib\h2-2.3.232.jar" IntegrationTest
```

The test is safe to run more than once: it reuses the "Test Integration"
customer and only performs transactions on the first run.

## 4. Results

```
=== Integration Test: full user flow ===

PASS - Database loads without errors
PASS - Register: customer is saved
PASS - Login: name + hashed PIN accepted
PASS - Open account: customer holds 3+ accounts
PASS - Transactions: deposit/withdraw/interest recorded
PASS - JDBC read: customer comes back from database
PASS - JDBC read: correct account subclasses restored
PASS - JDBC read: balances match the in-memory session
PASS - JDBC read: transaction history survives
PASS - Rules: savings interest = BWP0.50
PASS - Rules: investment interest = BWP40.00
PASS - Rules: cheque earns no interest

=== Result: 12 passed, 0 failed ===
```

## 5. Outcome

- **12 checks passed, 0 failed.**
- Business rules verified with real numbers: Savings paid 0.05%
  (BWP1000.00 → BWP1000.50), Investment paid 5%
  (BWP800.00 → BWP840.00), Cheque earned nothing (BWP1500.00).
- Data written through the DAOs was read back by a separate session,
  confirming the GUI → controller → domain → DAO → H2 chain is fully connected.
- The same database (`bankdata.mv.db`) is shared by the GUI and this test,
  so data entered through the screens is exactly what the DAOs persist.

## 6. Conclusion

All modules integrate correctly. The boundary classes collect input only,
controllers validate and orchestrate, the domain model enforces the banking
rules, and the DAOs persist everything to the H2 database. The system is
ready for the final presentation.
