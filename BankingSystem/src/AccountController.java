import javafx.scene.Scene;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller: AccountView talks to this class only.
 * This class is the only place the account screen calls deposit, withdraw, or interest.
 */
public class AccountController {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Account account;
    private final BankSession session;
    private final Navigator navigator;
    private AccountView view;

    public AccountController(Account account, BankSession session, Navigator navigator) {
        this.account = account;
        this.session = session;
        this.navigator = navigator;
    }

    public Scene show() {
        view = new AccountView(this);
        Scene scene = view.createScene();
        refresh();
        return scene;
    }

    public void handleDeposit(String amountText) {
        try {
            account.deposit(Input.amount(amountText));
            session.persist();
            view.clearAmount();
            refresh();
            view.showMessage("Deposit completed.", false);
        } catch (IllegalArgumentException ex) {
            view.showMessage(ex.getMessage(), true);
        }
    }

    public void handleWithdraw(String amountText) {
        try {
            if (!(account instanceof Withdrawable)) {
                view.showMessage("This account type does not allow withdrawals.", true);
                return;
            }
            boolean ok = ((Withdrawable) account).withdraw(Input.amount(amountText));
            if (ok) {
                session.persist();
            }
            view.clearAmount();
            refresh();
            view.showMessage(
                    ok ? "Withdrawal completed." : "Withdrawal refused (check the amount and balance).",
                    !ok
            );
        } catch (IllegalArgumentException ex) {
            view.showMessage(ex.getMessage(), true);
        }
    }

    public void handlePayInterest() {
        if (!(account instanceof InterestBearing)) {
            view.showMessage("This account type does not earn interest.", true);
            return;
        }
        ((InterestBearing) account).payInterest();
        session.persist();
        refresh();
        view.showMessage("Monthly interest applied.", false);
    }

    public void goBack() {
        navigator.showDashboard();
    }

    private void refresh() {
        view.showDetails(
                account.getAccountType(),
                account.getAccountNumber(),
                account.getBranch(),
                String.format(Locale.US, "BWP %,.2f", account.getBalance()),
                account instanceof Withdrawable,
                account instanceof InterestBearing,
                historyRows()
        );
    }

    private List<AccountView.TransactionRow> historyRows() {
        List<AccountView.TransactionRow> rows = new ArrayList<>();
        for (Transaction t : account.getTransactions()) {
            rows.add(new AccountView.TransactionRow(
                    t.getDateTime().format(TIME),
                    t.getType(),
                    String.format(Locale.US, "BWP %,.2f", t.getAmount()),
                    String.format(Locale.US, "BWP %,.2f", t.getBalanceAfter()),
                    t.getDescription()
            ));
        }
        return rows;
    }
}
