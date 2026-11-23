import javafx.scene.Scene;

/**
 * Controller: RegisterView talks to this class only.
 * This class creates the domain Customer and stores it in the session.
 */
public class RegisterController {
    private final BankSession session;
    private final Navigator navigator;
    private RegisterView view;

    public RegisterController(BankSession session, Navigator navigator) {
        this.session = session;
        this.navigator = navigator;
    }

    public Scene show() {
        view = new RegisterView(this);
        return view.createScene();
    }

    public void handleSubmit(String firstName, String surname, String address, String pin) {
        String first = Input.text(firstName);
        String last = Input.text(surname);
        String home = Input.text(address);
        String secret = Input.text(pin);
        if (first.isEmpty() || last.isEmpty()) {
            view.showError("First name and surname are required.");
            return;
        }
        if (session.findByName(first, last) != null) {
            view.showError("A customer with that name already exists.");
            return;
        }
        if (secret.length() < 4) {
            view.showError("PIN must be at least 4 digits.");
            return;
        }
        Customer created = new Customer(first, last, home);
        created.setPin(secret);
        session.addCustomer(created);
        session.persist();
        session.setCurrentCustomer(created);
        navigator.showDashboard();
    }

    public void goBack() {
        navigator.showLogin();
    }
}
