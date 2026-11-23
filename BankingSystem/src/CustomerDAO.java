import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the customers table.
 * CRUD: save() creates or reuses a row, findAll() reads.
 */
public class CustomerDAO {

    /**
     * Inserts the customer if they are not stored yet, otherwise reuses the
     * existing row. The customer's id is set on the object either way.
     */
    public static int save(Customer customer) throws SQLException {
        Connection con = DatabaseConnection.get();
        Integer existing = findId(customer, con);
        if (existing != null) {
            customer.setCustomerId(existing);
            // Keep the stored PIN in sync — it is saved hashed.
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE customers SET pin = ? WHERE customer_id = ?")) {
                ps.setString(1, customer.getPin());
                ps.setInt(2, existing);
                ps.executeUpdate();
            }
            return existing;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO customers (first_name, surname, address, pin) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getSurname());
            ps.setString(3, customer.getAddress());
            ps.setString(4, customer.getPin());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                customer.setCustomerId(id);
                return id;
            }
        }
    }

    private static Integer findId(Customer customer, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT customer_id FROM customers WHERE first_name = ? AND surname = ?"
                        + " AND address IS NOT DISTINCT FROM ?")) {
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getSurname());
            ps.setString(3, customer.getAddress());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("customer_id") : null;
            }
        }
    }

    /** Reads every stored customer (without accounts). */
    public static List<Customer> findAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        try (Statement st = DatabaseConnection.get().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT customer_id, first_name, surname, address, pin FROM customers"
                             + " ORDER BY customer_id")) {
            while (rs.next()) {
                Customer customer = new Customer(rs.getString("first_name"),
                        rs.getString("surname"), rs.getString("address"));
                String storedPin = rs.getString("pin");
                if (storedPin != null && storedPin.length() == 64) {
                    customer.setStoredPin(storedPin);   // already a SHA-256 hash
                } else if (storedPin != null) {
                    customer.setPin(storedPin);          // old plain-text PIN: hash it now
                }
                customer.setCustomerId(rs.getInt("customer_id"));
                customers.add(customer);
            }
        }
        return customers;
    }
}
