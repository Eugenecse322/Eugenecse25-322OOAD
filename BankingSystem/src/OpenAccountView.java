import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Boundary class: open-account form.
 * Talks only to OpenAccountController. Does not create account objects.
 */
public class OpenAccountView {
    private final OpenAccountController controller;
    private final ComboBox<String> typeBox = new ComboBox<>();
    private final TextField numberField = new TextField();
    private final TextField branchField = new TextField();
    private final TextField openingDepositField = new TextField();
    private final TextField companyNameField = new TextField();
    private final TextField companyAddressField = new TextField();
    private final VBox investmentBox = new VBox(8);
    private final VBox chequeBox = new VBox(8);
    private final Label messageLabel = new Label();

    public OpenAccountView(OpenAccountController controller) {
        this.controller = controller;
    }

    public Scene createScene() {
        typeBox.setItems(FXCollections.observableArrayList("Savings", "Investment", "Cheque"));
        typeBox.getSelectionModel().select("Savings");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setOnAction(e -> {
            refreshExtraFields();
            if (typeBox.getValue() != null) {
                controller.accountTypeChanged(typeBox.getValue());
            }
        });

        numberField.setPromptText("e.g. S-010");
        branchField.setPromptText("e.g. Main Mall");
        openingDepositField.setPromptText("Minimum BWP 500.00");
        companyNameField.setPromptText("Employer name");
        companyAddressField.setPromptText("Employer address");

        investmentBox.getChildren().setAll(
                UiTheme.fieldLabel("Opening deposit (BWP)"),
                openingDepositField
        );
        chequeBox.getChildren().setAll(
                UiTheme.fieldLabel("Company name"),
                companyNameField,
                UiTheme.fieldLabel("Company address"),
                companyAddressField
        );
        refreshExtraFields();

        Button openButton = UiTheme.primary("Open account");
        openButton.setDefaultButton(true);
        openButton.setOnAction(e -> controller.handleSubmit(
                typeBox.getValue(),
                numberField.getText(),
                branchField.getText(),
                openingDepositField.getText(),
                companyNameField.getText(),
                companyAddressField.getText()
        ));

        Button backButton = UiTheme.secondary("Back");
        backButton.setOnAction(e -> controller.goBack());

        VBox card = UiTheme.card(
                UiTheme.heading("Open an account"),
                UiTheme.muted("Choose a type, then fill in the details shown for that type."),
                UiTheme.fieldLabel("Account type"),
                typeBox,
                UiTheme.fieldLabel("Account number"),
                numberField,
                UiTheme.fieldLabel("Branch"),
                branchField,
                investmentBox,
                chequeBox,
                UiTheme.buttonRow(openButton, backButton),
                messageLabel
        );

        return UiTheme.scene(UiTheme.page("Pula Bank", "Open account", card), 820, 640);
    }

    public void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll("error-text");
    }

    public void suggestNumber(String number) {
        numberField.setText(number);
    }

    private void refreshExtraFields() {
        String type = typeBox.getValue();
        boolean investment = "Investment".equals(type);
        boolean cheque = "Cheque".equals(type);
        investmentBox.setVisible(investment);
        investmentBox.setManaged(investment);
        chequeBox.setVisible(cheque);
        chequeBox.setManaged(cheque);
    }
}
