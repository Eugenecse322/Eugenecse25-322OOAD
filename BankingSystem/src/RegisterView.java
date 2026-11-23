import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Boundary class: new customer form.
 * Talks only to RegisterController. Does not create a Customer itself.
 */
public class RegisterView {
    private final RegisterController controller;
    private final TextField firstNameField = new TextField();
    private final TextField surnameField = new TextField();
    private final TextField addressField = new TextField();
    private final PasswordField pinField = new PasswordField();
    private final Label messageLabel = new Label();

    public RegisterView(RegisterController controller) {
        this.controller = controller;
    }

    public Scene createScene() {
        firstNameField.setPromptText("First name");
        surnameField.setPromptText("Surname");
        addressField.setPromptText("Home address");
        pinField.setPromptText("Choose a PIN");

        Button saveButton = UiTheme.primary("Save customer");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(e -> controller.handleSubmit(
                firstNameField.getText(),
                surnameField.getText(),
                addressField.getText(),
                pinField.getText()
        ));

        Button backButton = UiTheme.secondary("Back to sign in");
        backButton.setOnAction(e -> controller.goBack());

        VBox card = UiTheme.card(
                UiTheme.heading("New customer"),
                UiTheme.muted("These details identify the person who will hold accounts."),
                UiTheme.fieldLabel("First name"),
                firstNameField,
                UiTheme.fieldLabel("Surname"),
                surnameField,
                UiTheme.fieldLabel("Address"),
                addressField,
                UiTheme.fieldLabel("PIN"),
                pinField,
                UiTheme.buttonRow(saveButton, backButton),
                messageLabel
        );

        return UiTheme.scene(UiTheme.page("Pula Bank", "Register customer", card), 780, 560);
    }

    public void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll("error-text");
    }
}
