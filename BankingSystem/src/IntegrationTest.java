/**
 * Integration test: runs one full user flow end-to-end.
 * register -> login -> open accounts -> deposit/withdraw/interest -> save -> reload from database.
 * Prints PASS or FAIL for each step. Safe to run more than once.
 */
public class IntegrationTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Integration Test: full user flow ===\n");

        BankSession session = new BankSession();
        session.loadFromDatabase();
        check("Database loads without errors", true);

        // STEP 1+2: register a customer, then "log in" (Register/Login controller logic)
        Customer customer = session.findByName("Test", "Integration");
        if (customer == null) {
            customer = new Customer("Test", "Integration", "1 Test Street");
            customer.setPin("9999");
            session.addCustomer(customer);
            session.persist();
        }
        check("Register: customer is saved", session.findByName("Test", "Integration") != null);

        Customer login = session.findByName("Test", "Integration");
        check("Login: name + hashed PIN accepted",
                login != null && PinHasher.hash("9999").equals(login.getPin()));

        // STEP 3: open one of each account type (OpenAccountController logic)
        boolean firstRun = session.findAccount("T-001") == null;
        if (firstRun) {
            new SavingsAccount("T-001", "Test Branch", login);
            new InvestmentAccount("T-002", "Test Branch", login, 500);
            new ChequeAccount("T-003", "Test Branch", login,
                    new EmploymentDetails("Test Corp", "1 Test Ave"));
            session.persist();
        }
        check("Open account: customer holds 3+ accounts", login.getAccounts().size() >= 3);

        // STEP 4: deposits, withdrawals, interest (AccountController logic)
        // Only on the first run so the test gives the same result every time.
        Account sav = session.findAccount("T-001");
        if (sav.getTransactions().isEmpty()) {
            sav.deposit(1000);
            session.findAccount("T-002").deposit(500);
            session.findAccount("T-003").deposit(2000);
            ((Withdrawable) session.findAccount("T-002")).withdraw(200);
            ((Withdrawable) session.findAccount("T-003")).withdraw(500);
            ((InterestBearing) sav).payInterest();
            ((InterestBearing) session.findAccount("T-002")).payInterest();
            session.persist();
        }
        check("Transactions: deposit/withdraw/interest recorded", sav.getTransactions().size() >= 2);

        // STEP 5: wipe memory and reload EVERYTHING through the DAOs
        BankSession reloaded = new BankSession();
        reloaded.loadFromDatabase();

        Customer again = reloaded.findByName("Test", "Integration");
        check("JDBC read: customer comes back from database", again != null);

        Account sav2 = reloaded.findAccount("T-001");
        Account inv2 = reloaded.findAccount("T-002");
        Account chq2 = reloaded.findAccount("T-003");
        check("JDBC read: correct account subclasses restored",
                sav2 instanceof SavingsAccount
                        && inv2 instanceof InvestmentAccount
                        && chq2 instanceof ChequeAccount);

        check("JDBC read: balances match the in-memory session",
                same(sav2, session.findAccount("T-001"))
                        && same(inv2, session.findAccount("T-002"))
                        && same(chq2, session.findAccount("T-003")));

        check("JDBC read: transaction history survives",
                sav2.getTransactions().size() == session.findAccount("T-001").getTransactions().size()
                        && sav2.getTransactions().size() >= 2);

        // Bonus check on first run: the actual business-rule numbers
        if (firstRun) {
            // sav: 1000 + 0.05% = 1000.50 | inv: 500+500-200+5% = 840.00 | chq: 2000-500 = 1500.00
            check("Rules: savings interest = BWP0.50", Math.abs(sav2.getBalance() - 1000.50) < 0.001);
            check("Rules: investment interest = BWP40.00", Math.abs(inv2.getBalance() - 840.00) < 0.001);
            check("Rules: cheque earns no interest", Math.abs(chq2.getBalance() - 1500.00) < 0.001);
        }

        System.out.println("\n=== Result: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static boolean same(Account a, Account b) {
        return a != null && b != null
                && Math.abs(a.getBalance() - b.getBalance()) < 0.001
                && a.getTransactions().size() == b.getTransactions().size();
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS" : "FAIL") + " - " + name);
        if (ok) {
            passed++;
        } else {
            failed++;
        }
    }
}
