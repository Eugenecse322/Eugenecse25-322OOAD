import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller: DashboardView talks to this class only.
 * This class reads Customer/Account from the session and starts the next controller.
 */
public class DashboardController {
    private final BankSession session;
    private final Navigator navigator;
    private DashboardView view;

    public DashboardController(BankSession session, Navigator navigator) {
        this.session = session;
        this.navigator = navigator;
    }

    public Scene show() {
        view = new DashboardView(this);
        Scene scene = view.createScene();
        refresh();
        return scene;
    }

    public void openAccount() {
        navigator.showOpenAccount();
    }

    public void viewAccount(String accountNumber) {
        Account account = session.findAccount(accountNumber);
        if (account == null) {
            return;
        }
        navigator.showAccount(new AccountController(account, session, navigator));
    }

    /** Pays monthly interest on every interest-bearing account, for every customer (UC-15). */
    public void payInterestForAll() {
        int paid = 0;
        for (Customer customer : session.getCustomers()) {
            for (Account account : customer.getAccounts()) {
                if (account instanceof InterestBearing) {
                    ((InterestBearing) account).payInterest();
                    paid++;
                }
            }
        }
        session.persist();
        refresh();
        view.showMessage(paid + " account(s) paid interest.");
    }

    public void logout() {
        session.setCurrentCustomer(null);
        navigator.showLogin();
    }

    private void refresh() {
        Customer customer = session.getCurrentCustomer();
        List<DashboardView.AccountRow> rows = new ArrayList<>();
        for (Account account : customer.getAccounts()) {
            String label = String.format(Locale.US, "%s  %s  —  BWP %,.2f",
                    account.getAccountType(), account.getAccountNumber(), account.getBalance());
            rows.add(new DashboardView.AccountRow(account.getAccountNumber(), label));
        }
        view.showCustomer(customer.getFullName(), customer.getAddress(), rows);
    }
}
