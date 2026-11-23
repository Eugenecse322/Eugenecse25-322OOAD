# Pula Bank — Banking System (Java + JavaFX)

A small banking system built in layers, for OOAD coursework.

## Folder layout
```
BankingSystem/
├── src/                  <- all Java source + schema.sql + bank.css
├── lib/javafx/           <- JavaFX jars (downloaded by setup script)
├── lib/h2-2.3.232.jar    <- H2 database driver
├── out/                  <- compiled classes (created automatically)
├── run-gui.bat           <- compile + launch the GUI
├── setup-javafx.ps1      <- downloads JavaFX jars (first run only)
├── LEARN.md              <- step-by-step manual for parts 1-6
├── NOTES.md
└── README.md
```

## Run it

**GUI (the full system):**
```
run-gui.bat
```
Demo login: **Eugene / Muskwe / PIN 4826**

**Console demo (domain classes only, no GUI/database):**
```
javac -d out src\Account.java src\SavingsAccount.java src\InvestmentAccount.java src\ChequeAccount.java src\Customer.java src\Transaction.java src\EmploymentDetails.java src\InterestBearing.java src\Withdrawable.java src\BankApp.java
java -cp out BankApp
```

**Database round-trip demo:**
```
javac -d out src\*.java
copy src\schema.sql out\
java -cp "out;lib\h2-2.3.232.jar" DatabaseDemo
```

## Where is the JavaFX code?

The editable JavaFX **source** files are in `src/`:

| File | What it is |
|---|---|
| `BankingFxApp.java` | App entry point — opens the window, switches screens |
| `LoginView.java` | Sign-in screen |
| `RegisterView.java` | New customer form |
| `DashboardView.java` | Home screen with the account list |
| `OpenAccountView.java` | Open-account form |
| `AccountView.java` | Account page (deposit/withdraw/interest + history) |
| `UiTheme.java` + `bank.css` | Shared styling so all screens look the same |

Do **not** try to edit the files in `lib/javafx/` — those are compiled
`.jar` libraries (the JavaFX engine itself), not source code. VS Code
cannot open them as text, and that's normal.

## Layers (see LEARN.md for details)
| Part | Layer | Files |
|---|---|---|
| 1 | Domain model | `Customer`, `Account`, `SavingsAccount`, `InvestmentAccount`, `ChequeAccount`, `EmploymentDetails`, `Transaction`, `InterestBearing`, `Withdrawable`, `BankApp` |
| 2 | Boundary (views) | `*View.java`, `UiTheme`, `bank.css` |
| 3 | Controllers | `*Controller.java`, `Navigator`, `Input` |
| 4 | Wiring | `BankSession`, `BankingFxApp` |
| 5 | Database schema | `schema.sql`, `DatabaseConnection` |
| 6 | DAOs | `CustomerDAO`, `AccountDAO`, `TransactionDAO`, `Database` |
