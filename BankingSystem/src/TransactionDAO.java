import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for the transactions table.
 * CRUD: saveAll() replaces an account's history, findAll() reads.
 */
public class TransactionDAO {

    /** Replaces the stored history of one account with its current in-memory one. */
    public static void saveAll(Account account) throws SQLException {
        Connection con = DatabaseConnection.get();
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM transactions WHERE account_number = ?")) {
            ps.setString(1, account.getAccountNumber());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO transactions (account_number, txn_type, amount, balance_after,"
                        + " description, txn_datetime) VALUES (?, ?, ?, ?, ?, ?)")) {
            for (Transaction t : account.getTransactions()) {
                ps.setString(1, account.getAccountNumber());
                ps.setString(2, t.getType());
                ps.setDouble(3, t.getAmount());
                ps.setDouble(4, t.getBalanceAfter());
                ps.setString(5, t.getDescription());
                ps.setObject(6, t.getDateTime());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Reads every stored transaction, grouped by account_number, oldest first. */
    public static Map<String, List<Transaction>> findAll() throws SQLException {
        Map<String, List<Transaction>> byAccount = new LinkedHashMap<>();
        try (Statement st = DatabaseConnection.get().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT account_number, txn_type, amount, balance_after, description,"
                             + " txn_datetime FROM transactions ORDER BY transaction_id")) {
            while (rs.next()) {
                byAccount.computeIfAbsent(rs.getString("account_number"), k -> new ArrayList<>())
                        .add(new Transaction(rs.getString("txn_type"), rs.getDouble("amount"),
                                rs.getDouble("balance_after"), rs.getString("description"),
                                rs.getTimestamp("txn_datetime").toLocalDateTime()));
            }
        }
        return byAccount;
    }
}
