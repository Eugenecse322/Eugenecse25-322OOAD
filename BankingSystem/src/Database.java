import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Facade over the DAOs. Knows how to save/load a whole customer (with their
 * accounts and transactions); all JDBC lives in the *DAO classes.
 */
public class Database {

    /** Opens the database and creates the schema if needed. */
    public static void init() throws SQLException {
        DatabaseConnection.get();
    }

    /** Stores one customer together with all of their accounts and transactions. */
    public static void saveCustomer(Customer customer) throws SQLException {
        int customerId = CustomerDAO.save(customer);
        for (Account account : customer.getAccounts()) {
            AccountDAO.save(account, customerId);
            TransactionDAO.saveAll(account);
        }
    }

    /** Loads every stored customer, accounts and transaction history included. */
    public static List<Customer> loadCustomers() throws SQLException {
        List<Customer> customers = CustomerDAO.findAll();
        List<Account> accounts = AccountDAO.findAll(customers);
        Map<String, List<Transaction>> histories = TransactionDAO.findAll();
        for (Account account : accounts) {
            // Balance was restored by AccountDAO; now attach the real history.
            account.restoreState(account.getBalance(),
                    histories.getOrDefault(account.getAccountNumber(), List.of()));
        }
        return customers;
    }

    public static void close() throws SQLException {
        DatabaseConnection.close();
    }
}
