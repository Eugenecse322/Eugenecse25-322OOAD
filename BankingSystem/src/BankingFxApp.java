import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point and Navigator.
 * Creates controllers and shows their scenes. It does not call domain methods.
 */
public class BankingFxApp extends Application implements Navigator {
    private final BankSession session = new BankSession();
    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("Pula Bank");
        session.loadFromDatabase();
        boolean seeded = false;
        if (session.getCustomers().isEmpty()) {
            session.seedDemoCustomer();   // first ever run: create the demo customer
            seeded = true;
        }
        if (session.findByName("Kabo", "Ramotswe") == null) {
            session.seedSampleData();     // sample records for the demo (once only)
            seeded = true;
        }
        if (seeded) {
            session.persist();
        }
        showLogin();
        stage.show();
    }

    @Override
    public void showLogin() {
        stage.setScene(new LoginController(session, this).show());
    }

    @Override
    public void showRegister() {
        stage.setScene(new RegisterController(session, this).show());
    }

    @Override
    public void showDashboard() {
        stage.setScene(new DashboardController(session, this).show());
    }

    @Override
    public void showOpenAccount() {
        stage.setScene(new OpenAccountController(session, this).show());
    }

    @Override
    public void showAccount(AccountController controller) {
        stage.setScene(controller.show());
    }
}
