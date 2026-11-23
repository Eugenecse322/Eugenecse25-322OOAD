import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for the accounts table.
 * CRUD: save() creates or updates, findAll() reads.
 */
public class AccountDAO {

    /** Saves the account by deleting the old row (if any) and inserting fresh. */
    public static void save(Account account, int customerId) throws SQLException {
        Connection con = DatabaseConnection.get();

        // Transactions reference accounts, so they must be deleted first (foreign key).
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM transactions WHERE account_number = ?")) {
            ps.setString(1, account.getAccountNumber());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM accounts WHERE account_number = ?")) {
            ps.setString(1, account.getAccountNumber());
            ps.executeUpdate();
        }

        ChequeAccount cheque = null;
        if (account instanceof ChequeAccount) {
            cheque = (ChequeAccount) account;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO accounts (account_number, account_type, branch, balance, customer_id,"
                        + " company_name, company_address) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, account.getAccountNumber());
            ps.setString(2, account.getAccountType());
            ps.setString(3, account.getBranch());
            ps.setDouble(4, account.getBalance());
            ps.setInt(5, customerId);
            ps.setString(6, cheque == null ? null : cheque.getEmployment().getCompanyName());
            ps.setString(7, cheque == null ? null : cheque.getEmployment().getCompanyAddress());
            ps.executeUpdate();
        }
    }

    /**
     * Reads every stored account and attaches it to its owner (the Account
     * constructor links both ways). Each account's stored balance is restored;
     * the transaction history is attached separately by Database.loadCustomers.
     */
    public static List<Account> findAll(List<Customer> customers) throws SQLException {
        Map<Integer, Customer> owners = new HashMap<>();
        for (Customer customer : customers) {
            owners.put(customer.getCustomerId(), customer);
        }
        List<Account> accounts = new ArrayList<>();
        try (Statement st = DatabaseConnection.get().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT account_number, account_type, branch, balance, customer_id,"
                             + " company_name, company_address FROM accounts")) {
            while (rs.next()) {
                Customer owner = owners.get(rs.getInt("customer_id"));
                if (owner == null) {
                    continue;
                }
                Account account = buildAccount(rs, owner);
                account.restoreState(rs.getDouble("balance"), List.of());
                accounts.add(account);
            }
        }
        return accounts;
    }

    private static Account buildAccount(ResultSet rs, Customer owner) throws SQLException {
        String number = rs.getString("account_number");
        String branch = rs.getString("branch");
        return switch (rs.getString("account_type")) {
            case "Investment" -> new InvestmentAccount(number, branch, owner,
                    InvestmentAccount.MIN_OPENING_DEPOSIT);
            case "Cheque" -> {
                String company = rs.getString("company_name");
                yield new ChequeAccount(number, branch, owner,
                        company == null ? null
                                : new EmploymentDetails(company, rs.getString("company_address")));
            }
            default -> new SavingsAccount(number, branch, owner);
        };
    }
}
