/** Test driver: runs every rule once so you can see the system working in the terminal. */
public class BankApp {
    public static void main(String[] args) {
        Customer c = new Customer("Thato", "Molefe", "Plot 123, Gaborone");
        System.out.println("Customer: " + c);

        heading("1. Open one of each account");
        SavingsAccount sav = new SavingsAccount("S-001", "Main Mall", c);
        InvestmentAccount inv = new InvestmentAccount("I-001", "Main Mall", c, 500.00);
        ChequeAccount chq = new ChequeAccount("C-001", "Main Mall", c,
                new EmploymentDetails("Acme (Pty) Ltd", "Private Bag 1, Gaborone"));
        c.getAccounts().forEach(System.out::println);

        heading("2. Deposits (overloaded method: with and without description)");
        sav.deposit(2000);
        inv.deposit(1000, "Top-up");
        chq.deposit(8500, "Salary");

        heading("3. Withdrawals");
        System.out.println("Investment withdraw 200 : " + (inv.withdraw(200) ? "OK" : "REFUSED"));
        System.out.println("Cheque withdraw 99999   : " + (chq.withdraw(99999) ? "OK" : "REFUSED (not enough money)"));
        // sav.withdraw(100);   // <-- remove the // and it will NOT compile: Savings has no withdraw()

        heading("4. Monthly interest (polymorphism: one loop, different behaviour)");
        for (Account a : c.getAccounts()) {
            if (a instanceof InterestBearing) {
                ((InterestBearing) a).payInterest();
            }
        }
        c.getAccounts().forEach(System.out::println);

        heading("5. Rules being enforced");
        try { new InvestmentAccount("I-002", "Main Mall", c, 100); }
        catch (IllegalArgumentException e) { System.out.println("Rejected: " + e.getMessage()); }

        try { new ChequeAccount("C-002", "Main Mall", c, null); }
        catch (IllegalArgumentException e) { System.out.println("Rejected: " + e.getMessage()); }

        try { new SavingsAccount("S-002", "Main Mall", null); }
        catch (IllegalArgumentException e) { System.out.println("Rejected: " + e.getMessage()); }

        try { sav.deposit(-50); }
        catch (IllegalArgumentException e) { System.out.println("Rejected: " + e.getMessage()); }

        System.out.println("Accounts still held by customer: " + c.getAccounts().size() + " (rejected ones were not added)");

        heading("6. Transaction history");
        sav.printHistory();
        inv.printHistory();
        chq.printHistory();
    }

    private static void heading(String text) {
        System.out.println("\n=== " + text + " ===");
    }
}
