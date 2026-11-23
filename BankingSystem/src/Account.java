import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ABSTRACT superclass for every account type.
 * Holds what all accounts share: number, branch, owner, balance, history.
 */
public abstract class Account {
    // ENCAPSULATION: every attribute is private
    private final String accountNumber;
    private final String branch;
    private final Customer owner;
    private double balance;
    private final List<Transaction> transactions = new ArrayList<>();

    protected Account(String accountNumber, String branch, Customer owner) {
        if (owner == null) {   // rule R2: no account without a customer
            throw new IllegalArgumentException("An account must be held by a customer");
        }
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.owner = owner;
        owner.addAccount(this);   // link both ways: customer <-> account
    }

    public String getAccountNumber() { return accountNumber; }
    public String getBranch()        { return branch; }
    public Customer getOwner()       { return owner; }
    public double getBalance()       { return balance; }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }

    // OVERLOADING: same name, different parameters
    public void deposit(double amount) {
        deposit(amount, "Deposit");
    }

    public void deposit(double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit must be more than zero");
        }
        balance += amount;
        transactions.add(new Transaction("DEPOSIT", amount, balance, description));
    }

    /** Only subclasses may take money out (protected). Returns false if refused. */
    protected boolean debit(double amount, String description) {
        if (amount <= 0 || amount > balance) {   // no overdrafts
            return false;
        }
        balance -= amount;
        transactions.add(new Transaction("WITHDRAWAL", amount, balance, description));
        return true;
    }

    /** Used by Database to rebuild stored state. Not a banking operation. */
    void restoreState(double balance, List<Transaction> history) {
        this.balance = balance;
        this.transactions.clear();
        this.transactions.addAll(history);
    }

    /** ABSTRACTION: each subclass must say what kind of account it is. */
    public abstract String getAccountType();

    public void printHistory() {
        System.out.println("History for " + accountNumber + ":");
        for (Transaction t : transactions) {
            System.out.println("  " + t);
        }
    }

    @Override   // OVERRIDING Object.toString()
    public String toString() {
        return String.format("%-10s [%s] branch=%s balance=BWP%.2f",
                getAccountType(), accountNumber, branch, balance);
    }
}
