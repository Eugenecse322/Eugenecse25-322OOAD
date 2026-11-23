import javafx.scene.Scene;

/**
 * Controller: LoginView talks to this class only.
 * This class looks up Customer in the session and asks the navigator to change screen.
 */
public class LoginController {
    private final BankSession session;
    private final Navigator navigator;
    private LoginView view;

    public LoginController(BankSession session, Navigator navigator) {
        this.session = session;
        this.navigator = navigator;
    }

    public Scene show() {
        view = new LoginView(this);
        return view.createScene();
    }

    public void handleLogin(String firstName, String surname, String pin) {
        String first = Input.text(firstName);
        String last = Input.text(surname);
        String secret = Input.text(pin);
        if (first.isEmpty() || last.isEmpty() || secret.isEmpty()) {
            view.showError("Enter first name, surname and PIN.");
            return;
        }
        if (session.loginIsLocked()) {
            view.showError("Too many failed attempts. Try again later.");
            return;
        }
        Customer customer = session.findByName(first, last);
        if (customer == null) {
            session.recordFailedLogin();
            view.showError("No customer found. Try Eugene Muskwe or create a new customer.");
            return;
        }
        // getPin() returns the SHA-256 hash, so hash what the user typed before comparing.
        if (!PinHasher.hash(secret).equals(customer.getPin())) {
            session.recordFailedLogin();
            view.showError("Wrong PIN.");
            return;
        }
        session.resetFailedLogins();
        session.setCurrentCustomer(customer);
        navigator.showDashboard();
    }

    public void goToRegister() {
        navigator.showRegister();
    }
}
