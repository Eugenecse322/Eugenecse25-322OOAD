import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;

/** Shared look-and-feel for every boundary screen. No business rules here. */
public final class UiTheme {
    private UiTheme() {}

    public static Scene scene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        URL bundled = UiTheme.class.getResource("bank.css");
        if (bundled != null) {
            scene.getStylesheets().add(bundled.toExternalForm());
        } else {
            File file = new File("src/bank.css");
            if (!file.exists()) {
                file = new File("BankingSystem/src/bank.css");
            }
            if (file.exists()) {
                scene.getStylesheets().add(file.toURI().toString());
            }
        }
        return scene;
    }

    public static BorderPane page(String title, String subtitle, Node center) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("app-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("app-subtitle");
        VBox titles = new VBox(2, titleLabel, subtitleLabel);

        HBox header = new HBox(titles);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox wrap = new VBox(center);
        wrap.getStyleClass().add("page");
        wrap.setAlignment(Pos.TOP_CENTER);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(wrap);
        return root;
    }

    public static VBox card(Node... children) {
        VBox card = new VBox(12, children);
        card.getStyleClass().add("card");
        card.setMaxWidth(560);
        return card;
    }

    public static Label heading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    public static Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted");
        label.setWrapText(true);
        return label;
    }

    public static Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    public static Button primary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary");
        return button;
    }

    public static Button secondary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary");
        return button;
    }

    public static HBox buttonRow(Node... buttons) {
        HBox row = new HBox(10, buttons);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
