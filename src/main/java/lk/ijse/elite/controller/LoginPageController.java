package lk.ijse.elite.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import lk.ijse.elite.bo.BOFactory;
import lk.ijse.elite.bo.custom.AppUserBO;

import java.io.IOException;

public class LoginPageController {

    @FXML public ImageView showPassword;

    @FXML private AnchorPane ancMainContainer;
    @FXML private Label lblSup1;
    @FXML private Button loginButton;

    @FXML private PasswordField passwordField;       // existing
    @FXML private TextField  passwordFieldVisible;   // NEW (plain text mirror)
    @FXML private TextField usernameField;

    private final AppUserBO appUserBO = BOFactory.getInstance().getBO(BOFactory.BOType.USER);
    private boolean showing = false;

    @FXML
    private void initialize() {
        // show the hidden plain text field only when toggled
        passwordFieldVisible.setVisible(false);
        passwordField.managedProperty().bind(passwordField.visibleProperty());
        passwordFieldVisible.managedProperty().bind(passwordFieldVisible.visibleProperty());

        // keep text in sync both ways
        passwordFieldVisible.textProperty().bindBidirectional(passwordField.textProperty());

        // ENTER on the visible text field should also submit
        passwordFieldVisible.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) signIn(); });
    }

    @FXML
    public void ShowPasswordOnAction(MouseEvent mouseEvent) {
        showing = !showing;

        passwordFieldVisible.setVisible(showing);
        passwordField.setVisible(!showing);

        if (showing) {
            passwordFieldVisible.requestFocus();
            passwordFieldVisible.positionCaret(passwordFieldVisible.getText().length());
            showPassword.setImage(new Image(getClass().getResource("/images/eye-off.png").toExternalForm()));
        } else {
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            showPassword.setImage(new Image(getClass().getResource("/images/eye.png").toExternalForm()));
        }
    }

    @FXML
    void forgotPasswordOnAction(MouseEvent event) { navigateTo("/view/ForgotPassword.fxml"); }

    @FXML
    public void onLogin(ActionEvent actionEvent) { signIn(); }

    @FXML
    public void onNext(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            (showing ? passwordFieldVisible : passwordField).requestFocus();
        }
    }

    @FXML
    public void onLog(KeyEvent keyEvent) { if (keyEvent.getCode() == KeyCode.ENTER) signIn(); }

    private void signIn() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = (showing ? passwordFieldVisible.getText() : passwordField.getText());
        if (username.isEmpty() || password.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Username and password are required", ButtonType.OK).show();
            return;
        }

        try {
            boolean ok = appUserBO.verifyLogin(username, password);
            if (ok) {
                String role = appUserBO.findRoleByLoginId(username);
                ancMainContainer.getChildren().clear();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
                AnchorPane pane = loader.load();
                DashboardController controller = loader.getController();
                controller.initForRole(role);
                pane.prefWidthProperty().bind(ancMainContainer.widthProperty());
                pane.prefHeightProperty().bind(ancMainContainer.heightProperty());
                ancMainContainer.getChildren().add(pane);
            } else {
                new Alert(Alert.AlertType.ERROR, "Invalid credentials!", ButtonType.OK).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).show();
        }
    }

    private void navigateTo(String path) {
        try {
            ancMainContainer.getChildren().clear();
            AnchorPane anchorPane = FXMLLoader.load(getClass().getResource(path));
            anchorPane.prefWidthProperty().bind(ancMainContainer.widthProperty());
            anchorPane.prefHeightProperty().bind(ancMainContainer.heightProperty());
            ancMainContainer.getChildren().add(anchorPane);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Page not found..!").show();
            e.printStackTrace();
        }
    }
}
