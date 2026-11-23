import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One line in an account's history. Immutable: nothing can be edited after creation. */
public class Transaction {
    private final LocalDateTime dateTime;
    private final String type;          // "DEPOSIT" or "WITHDRAWAL"
    private final double amount;
    private final double balanceAfter;
    private final String description;

    public Transaction(String type, double amount, double balanceAfter, String description) {
        this(type, amount, balanceAfter, description, LocalDateTime.now());
    }

    public Transaction(String type, double amount, double balanceAfter, String description,
                       LocalDateTime dateTime) {
        this.dateTime = dateTime;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    public String getType()          { return type; }
    public double getAmount()        { return amount; }
    public double getBalanceAfter()  { return balanceAfter; }
    public String getDescription()   { return description; }
    public LocalDateTime getDateTime() { return dateTime; }

    @Override
    public String toString() {
        return String.format("%s | %-10s | BWP%9.2f | balance BWP%9.2f | %s",
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                type, amount, balanceAfter, description);
    }
}
