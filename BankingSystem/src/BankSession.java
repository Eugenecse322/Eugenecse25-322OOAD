import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory session used by the controllers.
 * Holds the registered customers and who is currently signed in.
 */
public class BankSession {
    private final List<Customer> customers = new ArrayList<>();
    private Customer currentCustomer;
    private int failedLogins;   // consecutive wrong PINs; locks login at 3 (NFR-01)
    private int savingsCount = 1;
    private int investmentCount = 1;
    private int chequeCount = 1;

    public void seedDemoCustomer() {
        Customer eugene = new Customer("Eugene", "Muskwe", "Plot 123, Gaborone");
        eugene.setPin("4826");
        SavingsAccount sav = new SavingsAccount("S-001", "Main Mall", eugene);
        InvestmentAccount inv = new InvestmentAccount("I-001", "Main Mall", eugene, 500.00);
        ChequeAccount chq = new ChequeAccount("C-001", "Main Mall", eugene,
                new EmploymentDetails("Acme (Pty) Ltd", "Private Bag 1, Gaborone"));
        sav.deposit(2000);
        inv.deposit(1000, "Top-up");
        chq.deposit(8500, "Salary");
        customers.add(eugene);
        syncCounters();
    }

    /**
     * Extra sample customers so the database has ~10 records for the demo.
     * PINs are 1001-1005. Only called on the very first run (empty database).
     */
    public void seedSampleData() {
        Customer kabo = sample("Kabo", "Ramotswe", "Plot 45, Gaborone", "1001");
        new SavingsAccount("S-101", "Main Mall", kabo).deposit(1500);
        new ChequeAccount("C-101", "Main Mall", kabo,
                new EmploymentDetails("RDC Motors", "Gaborone")).deposit(3200);

        Customer neo = sample("Neo", "Kekeleetso", "Plot 12, Francistown", "1002");
        new InvestmentAccount("I-101", "Main Mall", neo, 800).deposit(200, "Top-up");
        new SavingsAccount("S-102", "Main Mall", neo).deposit(900);

        Customer onthatile = sample("Onthatile", "Gaborone", "Plot 7, Maun", "1003");
        new ChequeAccount("C-102", "Maun Branch", onthatile,
                new EmploymentDetails("Debswana", "Private Bag 1, Gaborone")).deposit(4500, "Salary");
        new InvestmentAccount("I-102", "Maun Branch", onthatile, 500);

        Customer kagiso = sample("Kagiso", "Molefe", "Plot 88, Selebi Phikwe", "1004");
        new SavingsAccount("S-103", "Main Mall", kagiso).deposit(750);
        new InvestmentAccount("I-103", "Main Mall", kagiso, 600);

        Customer mpho = sample("Mpho", "Tshwale", "Plot 3, Palapye", "1005");
        new ChequeAccount("C-103", "Main Mall", mpho,
                new EmploymentDetails("BMC", "Private Bag 5, Gaborone")).deposit(2800, "Salary");
        new SavingsAccount("S-104", "Main Mall", mpho).deposit(1100);

        syncCounters();
    }

    /** Helper: creates a customer with a PIN and adds it to the session. */
    private Customer sample(String first, String surname, String address, String pin) {
        Customer customer = new Customer(first, surname, address);
        customer.setPin(pin);
        customers.add(customer);
        return customer;
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    /** Saves every customer (with accounts and transactions) into the H2 database. */
    public void saveToDatabase() throws java.sql.SQLException {
        Database.init();
        for (Customer customer : customers) {
            Database.saveCustomer(customer);
        }
    }

    /** Replaces the in-memory customers with whatever is stored in the database. */
    public void loadFromDatabase() throws java.sql.SQLException {
        customers.clear();
        customers.addAll(Database.loadCustomers());
        syncCounters();
    }

    /** Saves everything to the database; prints a warning instead of throwing. */
    public void persist() {
        try {
            saveToDatabase();
        } catch (java.sql.SQLException e) {
            System.err.println("Could not save to database: " + e.getMessage());
        }
    }

    /** Resets the account-number counters to the highest numbers already in use. */
    private void syncCounters() {
        savingsCount = investmentCount = chequeCount = 0;
        for (Customer customer : customers) {
            for (Account account : customer.getAccounts()) {
                String number = account.getAccountNumber();
                int dash = number.indexOf('-');
                if (dash < 0) {
                    continue;
                }
                try {
                    int used = Integer.parseInt(number.substring(dash + 1));
                    switch (account.getAccountType()) {
                        case "Investment" -> investmentCount = Math.max(investmentCount, used);
                        case "Cheque" -> chequeCount = Math.max(chequeCount, used);
                        default -> savingsCount = Math.max(savingsCount, used);
                    }
                } catch (NumberFormatException ignored) {
                    // account numbers that do not end in -NNN are ignored
                }
            }
        }
    }

    public Customer findByName(String firstName, String surname) {
        String wanted = (firstName + " " + surname).trim();
        for (Customer customer : customers) {
            if (customer.getFullName().equalsIgnoreCase(wanted)) {
                return customer;
            }
        }
        return null;
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    /** True after 3 failed logins in a row — login stays blocked until one succeeds. */
    public boolean loginIsLocked() {
        return failedLogins >= 3;
    }

    public void recordFailedLogin() {
        failedLogins++;
    }

    public void resetFailedLogins() {
        failedLogins = 0;
    }

    public void setCurrentCustomer(Customer currentCustomer) {
        this.currentCustomer = currentCustomer;
    }

    public Account findAccount(String accountNumber) {
        for (Customer customer : customers) {
            for (Account account : customer.getAccounts()) {
                if (account.getAccountNumber().equals(accountNumber)) {
                    return account;
                }
            }
        }
        return null;
    }

    public String nextNumber(String type) {
        return switch (type) {
            case "Investment" -> "I-" + String.format("%03d", investmentCount + 1);
            case "Cheque" -> "C-" + String.format("%03d", chequeCount + 1);
            default -> "S-" + String.format("%03d", savingsCount + 1);
        };
    }

    public void rememberOpened(String type) {
        switch (type) {
            case "Investment" -> investmentCount++;
            case "Cheque" -> chequeCount++;
            default -> savingsCount++;
        }
    }
}
