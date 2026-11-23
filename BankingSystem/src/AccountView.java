import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Boundary class: one account's screen.
 * Talks only to AccountController. Receives already-formatted text, never an Account.
 */
public class AccountView {
    private final AccountController controller;
    private final Label typeLabel = new Label();
    private final Label numberLabel = UiTheme.muted("");
    private final Label branchLabel = UiTheme.muted("");
    private final Label balanceLabel = new Label();
    private final TextField amountField = new TextField();
    private final Button withdrawButton = UiTheme.secondary("Withdraw");
    private final Button interestButton = UiTheme.secondary("Pay monthly interest");
    private final TableView<TransactionRow> historyTable = new TableView<>();
    private final Label messageLabel = new Label();

    public AccountView(AccountController controller) {
        this.controller = controller;
    }

    public Scene createScene() {
        typeLabel.getStyleClass().add("section-title");
        balanceLabel.getStyleClass().add("balance");
        amountField.setPromptText("Amount in BWP");
        amountField.setPrefWidth(180);

        TableColumn<TransactionRow, String> dateCol = new TableColumn<>("When");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("when"));
        dateCol.setPrefWidth(150);
        TableColumn<TransactionRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(110);
        TableColumn<TransactionRow, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(110);
        TableColumn<TransactionRow, String> balanceCol = new TableColumn<>("Balance after");
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balanceAfter"));
        balanceCol.setPrefWidth(130);
        TableColumn<TransactionRow, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        noteCol.setPrefWidth(180);
        historyTable.getColumns().setAll(java.util.List.of(dateCol, typeCol, amountCol, balanceCol, noteCol));
        historyTable.setPrefHeight(220);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        Button depositButton = UiTheme.primary("Deposit");
        depositButton.setOnAction(e -> controller.handleDeposit(amountField.getText()));
        withdrawButton.setOnAction(e -> controller.handleWithdraw(amountField.getText()));
        interestButton.setOnAction(e -> controller.handlePayInterest());

        Button backButton = UiTheme.secondary("Back to accounts");
        backButton.setOnAction(e -> controller.goBack());

        HBox actions = UiTheme.buttonRow(amountField, depositButton, withdrawButton, interestButton);

        VBox card = UiTheme.card(
                typeLabel,
                numberLabel,
                branchLabel,
                balanceLabel,
                UiTheme.fieldLabel("Amount"),
                actions,
                messageLabel,
                UiTheme.fieldLabel("Transaction history"),
                historyTable,
                backButton
        );
        card.setMaxWidth(760);

        return UiTheme.scene(UiTheme.page("Pula Bank", "Account", card), 900, 680);
    }

    public void showDetails(String type, String number, String branch, String balanceText,
                            boolean canWithdraw, boolean canPayInterest,
                            List<TransactionRow> history) {
        typeLabel.setText(type + " account");
        numberLabel.setText("Number: " + number);
        branchLabel.setText("Branch: " + branch);
        balanceLabel.setText(balanceText);
        withdrawButton.setDisable(!canWithdraw);
        withdrawButton.setVisible(canWithdraw);
        withdrawButton.setManaged(canWithdraw);
        interestButton.setDisable(!canPayInterest);
        interestButton.setVisible(canPayInterest);
        interestButton.setManaged(canPayInterest);
        historyTable.setItems(FXCollections.observableArrayList(history));
    }

    public void showMessage(String message, boolean error) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll(error ? "error-text" : "success-text");
    }

    public void clearAmount() {
        amountField.clear();
    }

    /** One history line for the table. JavaFX reads the getters by property name. */
    public static class TransactionRow {
        private final String when;
        private final String type;
        private final String amount;
        private final String balanceAfter;
        private final String note;

        public TransactionRow(String when, String type, String amount, String balanceAfter, String note) {
            this.when = when;
            this.type = type;
            this.amount = amount;
            this.balanceAfter = balanceAfter;
            this.note = note;
        }

        public String getWhen() { return when; }
        public String getType() { return type; }
        public String getAmount() { return amount; }
        public String getBalanceAfter() { return balanceAfter; }
        public String getNote() { return note; }
    }
}
