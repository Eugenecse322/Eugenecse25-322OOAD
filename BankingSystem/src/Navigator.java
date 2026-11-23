/**
 * Screen switching used by controllers.
 * The navigator does not call domain classes; controllers do that first.
 */
public interface Navigator {
    void showLogin();
    void showRegister();
    void showDashboard();
    void showOpenAccount();
    void showAccount(AccountController controller);
}
