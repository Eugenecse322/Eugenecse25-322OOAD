# NOTES – understanding the code

## 1. The big picture
The class diagram has 10 pieces. Each one is one `.java` file in `src/`.

| File | What it is | One-line purpose |
|---|---|---|
| `Customer` | class | Person who owns accounts (first name, surname, address) |
| `Account` | **abstract** class | Everything all accounts share |
| `SavingsAccount` | class (extends Account) | Deposits only, 0.05% monthly interest |
| `InvestmentAccount` | class (extends Account) | Min BWP500 to open, deposits + withdrawals, 5% interest |
| `ChequeAccount` | class (extends Account) | Deposits + withdrawals, working people only, no interest |
| `EmploymentDetails` | class | Company name + address (needed by Cheque) |
| `Transaction` | class | One history line: date, type, amount, balance after |
| `InterestBearing` | **interface** | Promise: "I can pay interest" |
| `Withdrawable` | **interface** | Promise: "I allow withdrawals" |
| `BankApp` | class with `main` | Test program that shows it all working |

## 2. Who can do what
| | deposit | withdraw | interest |
|---|---|---|---|
| Savings | yes | **no** | 0.05% |
| Investment | yes | yes | 5% |
| Cheque | yes | yes | none |

This is done with the two interfaces:
- Savings implements `InterestBearing` only.
- Investment implements `InterestBearing` **and** `Withdrawable`.
- Cheque implements `Withdrawable` only.

Savings has no `withdraw()` method, so `sav.withdraw(100)` will not even compile.

## 3. Business rules -> where they live in the code
| Rule | Where |
|---|---|
| Account cannot exist without a customer | `Account` constructor throws an error if `owner == null` |
| Investment needs BWP500.00 to open | `InvestmentAccount.checkOpeningDeposit()` |
| Cheque only for working people | `ChequeAccount.checkEmployment()` (needs `EmploymentDetails`) |
| No negative / zero deposits | `Account.deposit()` |
| No overdrafts | `Account.debit()` returns `false` if amount > balance |
| Interest 5% / 0.05%, rounded to 2 decimals | `payInterest()` in Investment / Savings |
| Customer can hold many accounts | `Customer` has a list of accounts |

## 4. The OOP words your marker looks for (and where to point)
| Concept | Example in this code |
|---|---|
| **Abstraction** | `Account` is `abstract`; `getAccountType()` has no body, subclasses must supply it |
| **Inheritance** | `SavingsAccount extends Account` (also Investment and Cheque) |
| **Interface** | `InterestBearing`, `Withdrawable` |
| **Encapsulation** | All attributes are `private`; use getters. `debit()` is `protected` so only subclasses use it |
| **Overloading** | `deposit(amount)` and `deposit(amount, description)` in `Account` (same name, different parameters) |
| **Overriding** | `getAccountType()`, `payInterest()`, `withdraw()`, `toString()` (same signature, new behaviour) |
| **Polymorphism** | In `BankApp` step 4: one loop calls `payInterest()` on every account and each pays its own rate |
| **Composition** | An account is created *with* its customer; Cheque is created *with* its EmploymentDetails |

## 5. Walk-through of the key lines

**`Account` constructor**
```java
owner.addAccount(this);
```
When an account is created, it adds itself to its customer's list. That is how one customer ends up with several accounts.

**`deposit` (in Account)** – checks amount > 0, adds to balance, saves a `Transaction`.

**`debit` (in Account)** – refuses if the amount is zero/negative or bigger than the balance. Otherwise subtracts and saves a `Transaction`. `Investment.withdraw()` and `Cheque.withdraw()` simply call it.

**`super(accountNumber, branch, checkOpeningDeposit(owner, openingDeposit))`**
Java forces `super(...)` to be the first line, so the rule check is run *inside* it. This means a rejected account is never created and never added to the customer's list.

**`payInterest`**
```java
double interest = Math.round(getBalance() * MONTHLY_RATE * 100) / 100.0;
```
Balance x rate, rounded to 2 decimals (`*100`, round, `/100`).
Example: 2000 x 0.0005 = BWP1.00 and 1300 x 0.05 = BWP65.00.

## 6. Expected output (key lines)
- Savings after interest: `BWP2001.00`
- Investment: 500 + 1000 - 200 = 1300, plus 5% = `BWP1365.00`
- Cheque: `BWP8500.00` (no interest)
- Four "Rejected:" messages in section 5, and the customer still has 3 accounts.

## 7. Things to say if your lecturer asks
- *Why `double`?* Simple for this part. A real bank would use `BigDecimal` to avoid tiny rounding errors.
- *Why interfaces instead of putting `withdraw()` in Account?* Because Savings must not allow withdrawals; an interface means Savings simply does not have the method.
- *Why no GUI / database?* The assignment says to focus on attributes and basic business logic only.

## 8. Try it yourself (good for understanding)
1. In `BankApp`, remove the `//` before `sav.withdraw(100);` and compile. Read the error.
2. Change `500.00` to `400.00` in the Investment constructor call and run. See the rejection.
3. Add a second `Customer` and give them a Savings account.
