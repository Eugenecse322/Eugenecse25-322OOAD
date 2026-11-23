import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Boundary class: login screen.
 * Talks only to LoginController. Does not touch Customer or other domain classes.
 */
public class LoginView {
    private final LoginController controller;
    private final TextField firstNameField = new TextField();
    private final TextField surnameField = new TextField();
    private final PasswordField pinField = new PasswordField();
    private final Label messageLabel = new Label();

    public LoginView(LoginController controller) {
        this.controller = controller;
    }

    public Scene createScene() {
        firstNameField.setPromptText("First name");
        surnameField.setPromptText("Surname");
        pinField.setPromptText("PIN");

        Button loginButton = UiTheme.primary("Sign in");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> controller.handleLogin(
                firstNameField.getText(), surnameField.getText(), pinField.getText()));

        Button registerButton = UiTheme.secondary("Create customer");
        registerButton.setOnAction(e -> controller.goToRegister());

        VBox card = UiTheme.card(
                UiTheme.heading("Welcome"),
                UiTheme.muted("Sign in with the customer's name and PIN."),
                UiTheme.fieldLabel("First name"),
                firstNameField,
                UiTheme.fieldLabel("Surname"),
                surnameField,
                UiTheme.fieldLabel("PIN"),
                pinField,
                UiTheme.buttonRow(loginButton, registerButton),
                messageLabel,
                UiTheme.muted("Demo customer: Eugene Muskwe, PIN 4826")
        );
        card.setAlignment(Pos.CENTER_LEFT);

        return UiTheme.scene(UiTheme.page("Pula Bank", "Customer banking", card), 780, 520);
    }

    public void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll("error-text");
    }
}
