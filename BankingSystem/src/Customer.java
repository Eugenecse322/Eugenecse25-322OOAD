import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A bank customer. One customer holds 1..* accounts (rule R1). */
public class Customer {
    private int customerId;   // assigned by CustomerDAO when saved
    private final String firstName;
    private final String surname;
    private String address;
    private String pin;   // secret used to sign in
    private final List<Account> accounts = new ArrayList<>();

    public Customer(String firstName, String surname, String address) {
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
    }

    // No "public": only classes in this folder (i.e. Account) can call it.
    void addAccount(Account account) { accounts.add(account); }

    public List<Account> getAccounts() { return Collections.unmodifiableList(accounts); }
    public int getCustomerId()        { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getFirstName()       { return firstName; }
    public String getSurname()         { return surname; }
    public String getFullName()        { return firstName + " " + surname; }
    public String getAddress()         { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPin()               { return pin; }

    /** Stores a plain-text PIN as its SHA-256 hash. */
    public void setPin(String pin)       { this.pin = pin == null ? null : PinHasher.hash(pin); }

    /** Package-private: stores an already-hashed PIN loaded from the database. */
    void setStoredPin(String hash)       { this.pin = hash; }

    @Override
    public String toString() { return getFullName() + " (" + address + ")"; }
}
