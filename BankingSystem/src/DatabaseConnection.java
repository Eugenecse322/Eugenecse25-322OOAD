import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single H2 connection (jdbc:h2:file:./bankdata — data persists
 * in bankdata.mv.db between runs). The schema lives in src/schema.sql.
 */
public class DatabaseConnection {
    // AUTO_SERVER lets the GUI and tools like IntegrationTest share the file at once.
    private static final String URL = "jdbc:h2:file:./bankdata;AUTO_SERVER=TRUE";
    private static Connection connection;

    /** Returns the shared connection, opening it and running the schema if needed. */
    public static Connection get() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, "sa", "");
            runSchema();
        }
        return connection;
    }

    private static void runSchema() throws SQLException {
        try (InputStream in = schemaStream();
             Statement st = connection.createStatement()) {
            String sql = new String(in.readAllBytes());
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    st.execute(trimmed);
                }
            }
            // Widen pin for databases created before PINs were hashed (64-char SHA-256 hex).
            try {
                st.execute("ALTER TABLE customers ALTER COLUMN pin VARCHAR(64)");
            } catch (SQLException ignored) {
                // column is already wide enough, or the table was just created
            }
        } catch (IOException e) {
            throw new SQLException("Could not read schema.sql", e);
        }
    }

    private static InputStream schemaStream() throws IOException {
        InputStream in = DatabaseConnection.class.getResourceAsStream("/schema.sql");
        if (in == null) {   // fallback when running straight from src/
            in = Files.newInputStream(Path.of("src", "schema.sql"));
        }
        return in;
    }

    public static void close() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}
