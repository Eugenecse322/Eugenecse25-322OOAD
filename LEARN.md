# Pula Bank — Instruction Manual (Parts 1–6)

This manual walks you through the code layer by layer so you can learn it.
Read the parts **in order** — each one builds on the previous.

## The big picture

```
        ┌─────────────────────────────────────────────┐
        │  PART 2  Views (boundary classes)           │
        │  LoginView  RegisterView  DashboardView ... │
        │  "show info + collect input, nothing else"  │
        └──────────────────┬──────────────────────────┘
                           │ calls only
                           ▼
        ┌─────────────────────────────────────────────┐
        │  PART 3  Controllers                        │
        │  LoginController  AccountController ...     │
        │  "validate input, call the domain"          │
        └──────────────────┬──────────────────────────┘
                           │ uses
                           ▼
        ┌─────────────────────────────────────────────┐
        │  PART 1  Domain model (the real banking)    │
        │  Customer  Account  Savings/Investment/     │
        │  Cheque  Transaction  InterestBearing ...   │
        └──────────────────┬──────────────────────────┘
                           │ saved/loaded via
                           ▼
        ┌─────────────────────────────────────────────┐
        │  PART 6  DAOs  →  PART 5  Database (H2)     │
        │  CustomerDAO  AccountDAO  TransactionDAO    │
        └─────────────────────────────────────────────┘
```

**Golden rule of the whole design:** information flows one way.
View → Controller → Domain → DAO → Database.
A View never touches `Customer` or `Account`. A Controller never builds UI widgets.

---

## PART 1 — Domain model classes

**Goal:** turn the class diagram into working code. Only attributes and
business logic (deposit, withdraw, interest). No GUI, no database.

**Files:** `Customer.java`, `Account.java`, `SavingsAccount.java`,
`InvestmentAccount.java`, `ChequeAccount.java`, `EmploymentDetails.java`,
`Transaction.java`, `InterestBearing.java`, `Withdrawable.java`, `BankApp.java`

### The classes, one by one

**`Customer.java`** — a person who holds accounts.
- Attributes: `firstName`, `surname`, `address`, `pin`, `accounts` (a list),
  `customerId` (only used by the database layer — ignore it for now).
- Key method: `addAccount()` is **package-private** (no `public`), so only
  classes in the same folder — i.e. `Account` — can call it. That is
  encapsulation in action.
- `getAccounts()` returns an **unmodifiable** list: callers can look but
  not add/remove behind the customer's back.

**`Account.java`** — `abstract`, the parent of all account types.
- Attributes: `accountNumber`, `branch`, `owner` (a `Customer`), `balance`,
  `transactions` (history).
- The constructor **throws** if `owner` is null → rule R2: no account
  without a customer. It also calls `owner.addAccount(this)` so the link
  works both ways.
- `deposit(amount)` / `deposit(amount, description)` — **overloading**:
  same name, different parameters. The short version just calls the long one.
- `debit(amount, description)` is **protected**: only subclasses can take
  money out, and only if `amount <= balance` (no overdrafts).
- `getAccountType()` is **abstract** — every subclass MUST say what it is.
- Every deposit/withdrawal creates a `Transaction` and adds it to history.

**`SavingsAccount.java`** — implements `InterestBearing` only.
- 0.05% monthly interest (`MONTHLY_RATE = 0.0005`).
- It does **NOT** implement `Withdrawable`, so `sav.withdraw(...)` will not
  even compile. Withdrawals are impossible *by design*, not by an `if`.

**`InvestmentAccount.java`** — implements both interfaces.
- Minimum opening deposit BWP500.00. The check runs in a `static` method
  *before* `super()`, so a rejected account never gets added to the
  customer's list (the guide's own example has this bug — ours doesn't).
- 5% monthly interest.

**`ChequeAccount.java`** — implements `Withdrawable` only (no interest).
- Requires an `EmploymentDetails` object — rule R5: only working people.

**`EmploymentDetails.java`** — just `companyName` + `companyAddress`.
A cheque account cannot exist without one (composition ◆).

**`Transaction.java`** — one history line. **Immutable**: all fields are
`final`, set once in the constructor, never changed.

**`InterestBearing.java` / `Withdrawable.java`** — interfaces = contracts.
Any account that "can" do a thing implements the interface; accounts that
can't simply don't have the method.

**`BankApp.java`** — a `main` method that exercises every rule once and
prints the results. This is your Part-1 proof that the code works.

### Try it
```
javac -d out src\Account.java src\SavingsAccount.java src\InvestmentAccount.java src\ChequeAccount.java src\Customer.java src\Transaction.java src\EmploymentDetails.java src\InterestBearing.java src\Withdrawable.java src\BankApp.java
java -cp out BankApp
```

### Mini-exercises (do these to really learn it)
1. In `BankApp`, comment out `sav.deposit(2000);` and predict the balance
   before running. Then run it.
2. Try `sav.withdraw(100);` — it should **not compile**. That is the
   interface doing its job.
3. Change `MONTHLY_RATE` in `SavingsAccount` to `0.01` (1%) and see the
   interest change.
4. Find where `balance` is declared `private` in `Account`. Try adding
   `account.balance = 0;` in `BankApp` — it will not compile.

---

## PART 2 — Boundary classes (the GUI screens)

**Goal:** JavaFX screens. Boundary classes only *display* information and
*collect* input. **No business logic allowed in them.**

**Files:** `*View.java` (one per screen), `UiTheme.java` (shared styling),
`bank.css` (colours), `BankingFxApp.java` (starts the app).

| View | Screen | Collects |
|---|---|---|
| `LoginView` | Sign in | first name, surname, PIN |
| `RegisterView` | New customer | name, address, PIN |
| `DashboardView` | Home | shows accounts, buttons to open/view |
| `OpenAccountView` | Open account form | type, number, branch, deposit, employer |
| `AccountView` | One account | amount + Deposit/Withdraw/Interest buttons, history table |

**The pattern every View follows:**
```java
public class LoginView {
    private final LoginController controller;   // talks ONLY to its controller
    private final TextField firstNameField = new TextField();
    ...
    loginButton.setOnAction(e ->
        controller.handleLogin(firstNameField.getText(), ...));  // hand raw text over
}
```

Notice: the view passes `getText()` strings to the controller. It never
creates a `Customer`, never checks a PIN, never decides anything.
That's the whole point of a boundary class.

`UiTheme` is just helper methods (`primary()`, `card()`, `fieldLabel()`…)
so every screen looks the same. `BankingFxApp` is the `Application` —
it opens the window and acts as the `Navigator`.

---

## PART 3 — Controller classes

**Goal:** the middle-man. Controllers take what the view collected,
**validate it**, call the **domain**, and tell the navigator which screen
to show next.

**Files:** `LoginController`, `RegisterController`, `DashboardController`,
`OpenAccountController`, `AccountController`, plus helpers `Input.java`
(text/amount parsing) and `Navigator.java` (the screen-switching contract).

**Example — the login flow, start to finish:**

1. User clicks "Sign in" in `LoginView`.
2. View calls `controller.handleLogin(first, surname, pin)`.
3. `LoginController` trims the input (`Input.text`), checks nothing is
   empty, asks `session.findByName(...)` for the `Customer`, compares
   `customer.getPin()`, and on success calls `session.setCurrentCustomer(...)`
   then `navigator.showDashboard()`.
4. If anything fails, it calls `view.showError("Wrong PIN.")` — the view
   only displays the message.

Controllers are where the rules live:
- `LoginController` — name + PIN check, and locks login after 3 failed
  attempts in a row (the counter lives in `BankSession`). PINs are never
  stored as plain text: `Customer.setPin()` hashes them with SHA-256 via
  `PinHasher`, and login hashes what you type before comparing.
- `RegisterController` — name required, no duplicates, PIN ≥ 4 chars.
- `OpenAccountController` — number+branch required, number not already
  used, cheque needs employer details, then `new SavingsAccount(...)` etc.
- `AccountController` — the ONLY place the GUI calls `deposit()`,
  `withdraw()`, `payInterest()`. It also checks `instanceof Withdrawable`
  before offering a withdrawal.
- `DashboardController.payInterestForAll()` — the "Pay interest to all
  accounts" button: loops every customer and every account, calls
  `payInterest()` on each `InterestBearing` one. One call, different
  behaviour per account type — polymorphism in action (UC-15).

`BankSession` (Part 4) is what controllers use instead of touching the
database or static lists themselves.

---

## PART 4 — Connecting the layers

**Goal:** views talk only to controllers; controllers talk only to the
domain. Nothing skips a layer.

**The wiring files:**
- `Navigator.java` — an **interface** listing the screens:
  `showLogin()`, `showDashboard()`, `showAccount(...)`, etc.
- `BankingFxApp` — `implements Navigator`. It owns the `BankSession`,
  creates each controller, and swaps the window's `Scene`.
- `BankSession` — the "current state" of the app: the list of customers,
  who is signed in, and the account-number counters. Controllers ask it
  questions (`findByName`, `findAccount`, `nextNumber`) instead of keeping
  their own lists. After anything changes, a controller calls
  `session.persist()` — one line that saves the whole session through the
  DAOs into the database.

**Who is allowed to call what:**

| Class | May call |
|---|---|
| `*View` | only its `*Controller` |
| `*Controller` | `BankSession`, domain classes, `Navigator`, `Input` |
| `BankSession` | domain constructors/getters |
| `Account`/`Customer` | each other (and `Transaction`) |

If you ever see a `View` importing `Customer` or an `Account`, the layers
are broken — that's what this part is checking.

**Trace one button press:** Dashboard "Open account" →
`DashboardView` → `DashboardController.openAccount()` →
`navigator.showOpenAccount()` → `BankingFxApp` creates
`OpenAccountController` + `OpenAccountView` → user submits →
controller builds the `Account` → `navigator.showDashboard()` refreshes.

---

## PART 5 — Database schema

**Goal:** an H2 database with tables for the core classes. It is a normal
file (`bankdata.mv.db`) so data survives between runs.

**Files:** `src/schema.sql`, `DatabaseConnection.java`, `lib/h2-2.3.232.jar`

`schema.sql` creates three tables:

```sql
customers    (customer_id, first_name, surname, address, pin)
accounts     (account_id, account_number UNIQUE, account_type
              CHECK IN ('Savings','Investment','Cheque'), branch,
              balance, customer_id -> customers, company_name/address)
transactions (transaction_id, account_number -> accounts, txn_type,
              amount, balance_after, description, txn_datetime)
```

How it maps to the classes:
- `Customer` → `customers` row
- `Account` (+ its subclass type) → `accounts` row; `account_type` column
  remembers which subclass it was
- `Transaction` → `transactions` rows, many per account
- Cheque employer details ride along as `company_name`/`company_address`

`DatabaseConnection` opens `jdbc:h2:file:./bankdata` — a real file, so
your customers are still there after you close and reopen the app.
It runs `schema.sql` automatically the first time.

---

## PART 6 — DAOs (data access objects)

**Goal:** all JDBC code lives in DAO classes. One DAO per table.

**Files:** `CustomerDAO.java`, `AccountDAO.java`, `TransactionDAO.java`,
`Database.java` (thin coordinator — contains NO SQL itself).

| DAO | Methods | CRUD it covers |
|---|---|---|
| `CustomerDAO` | `save()`, `findAll()` | Create/Read customers |
| `AccountDAO` | `save()`, `findAll()` | Create/Read accounts (save = delete old row + insert fresh) |
| `TransactionDAO` | `saveAll()`, `findAll()` | Create/Read transactions (delete + re-insert per account) |

`Database` is the only class controllers/session should call:

```java
Database.saveCustomer(customer);          // CustomerDAO + AccountDAO + TransactionDAO
List<Customer> all = Database.loadCustomers();  // reads everything back
```

`BankSession.saveToDatabase()` / `loadFromDatabase()` are the convenience
wrappers the app uses.

**Try it:**
```
javac -d out src\*.java
copy src\schema.sql out\
java -cp "out;lib\h2-2.3.232.jar" DatabaseDemo
```
You should see Eugene Muskwe reload with all 3 accounts and full history.

---

## How to study this code (suggested order)

1. Read `Account.java` + the three subclasses until `deposit`/`withdraw`/
   `payInterest` make sense. Do the Part-1 exercises.
2. Read `BankApp.java` output and match each line to a rule (R1–R5).
3. Read `LoginView` → `LoginController` → `BankSession.findByName` as ONE
   story, top to bottom.
4. Open `DashboardView` and check: does it import any domain class? It
   shouldn't — only `DashboardController`.
5. Read `schema.sql`, then `CustomerDAO` — notice every `?` is a value
   plugged in safely (no string-splicing of SQL).
6. Run the GUI (`run-gui.bat`), sign in as Eugene Muskwe / 4826, and click
   through while tracing which controller handles each button.

## Quick glossary

- **Encapsulation** — fields are `private`; change data only through methods.
- **Inheritance** — `SavingsAccount extends Account` reuses number/balance.
- **Interface** — a contract (`Withdrawable`); implements = "can do this".
- **Polymorphism** — `for (Account a : ...) if (a instanceof InterestBearing)`
  calls different `payInterest()` per account type.
- **Overloading** — `deposit(x)` vs `deposit(x, "Salary")`: same name,
  different parameters.
- **Composition (◆)** — a part can't exist alone (account needs a customer;
  cheque needs employer details).
- **DAO** — class that hides all SQL for one table.
- **Boundary class** — a View: shows and collects, decides nothing.
