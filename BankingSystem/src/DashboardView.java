import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Boundary class: customer home screen.
 * Talks only to DashboardController. Account rows are display text, not domain objects.
 */
public class DashboardView {
    private final DashboardController controller;
    private final Label welcomeLabel = new Label();
    private final Label addressLabel = UiTheme.muted("");
    private final ListView<AccountRow> accountList = new ListView<>();
    private final Label messageLabel = new Label();

    public DashboardView(DashboardController controller) {
        this.controller = controller;
    }

    public Scene createScene() {
        welcomeLabel.getStyleClass().add("section-title");
        accountList.setPrefHeight(220);
        accountList.setPlaceholder(new Label("No accounts yet"));

        Button openButton = UiTheme.primary("Open account");
        openButton.setOnAction(e -> controller.openAccount());

        Button viewButton = UiTheme.secondary("View selected");
        viewButton.setOnAction(e -> fireView());
        accountList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                fireView();
            }
        });

        Button interestButton = UiTheme.secondary("Pay interest to all accounts");
        interestButton.setOnAction(e -> controller.payInterestForAll());

        Button logoutButton = new Button("Sign out");
        logoutButton.getStyleClass().add("link-button");
        logoutButton.setOnAction(e -> controller.logout());

        VBox card = UiTheme.card(
                welcomeLabel,
                addressLabel,
                UiTheme.fieldLabel("Accounts"),
                accountList,
                UiTheme.buttonRow(openButton, viewButton),
                UiTheme.buttonRow(interestButton, logoutButton),
                messageLabel,
                UiTheme.muted("Double-click an account to open it.")
        );
        card.setMaxWidth(640);

        return UiTheme.scene(UiTheme.page("Pula Bank", "Customer dashboard", card), 820, 580);
    }

    public void showCustomer(String fullName, String address, List<AccountRow> accounts) {
        welcomeLabel.setText("Hello, " + fullName);
        addressLabel.setText(address);
        accountList.setItems(FXCollections.observableArrayList(accounts));
        if (!accounts.isEmpty()) {
            accountList.getSelectionModel().selectFirst();
        }
    }

    public void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll("success-text");
    }

    private void fireView() {
        AccountRow selected = accountList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            controller.viewAccount(selected.accountNumber);
        }
    }

    /** Display row for the list. The view never holds an Account object. */
    public static class AccountRow {
        public final String accountNumber;
        public final String label;

        public AccountRow(String accountNumber, String label) {
            this.accountNumber = accountNumber;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
