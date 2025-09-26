package lk.ijse.elite.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import lk.ijse.elite.bo.BOFactory;
import lk.ijse.elite.bo.BOFactory.BOType;
import lk.ijse.elite.bo.custom.AppUserBO;

import java.io.IOException;

public class LoginPageController {

    @FXML private AnchorPane ancMainContainer;
    @FXML private Label lblSup1;
    @FXML private Button loginButton;
    @FXML private PasswordField passwordField;
    @FXML private TextField usernameField;

    // Use BO (Hibernate), no Model/JDBC
    private final AppUserBO appUserBO = BOFactory.getInstance().getBO(BOType.USER);

    @FXML
    void forgotPasswordOnAction(MouseEvent event) {
        navigateTo("/view/ForgotPassword.fxml");
    }

    @FXML
    public void onLogin(ActionEvent actionEvent) {
        signIn();
    }

    @FXML
    public void onNext(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            passwordField.requestFocus();
        }
    }

    @FXML
    public void onLog(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            signIn();
        }
    }

    private void signIn() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Username and password are required", ButtonType.OK).show();
            return;
        }

        try {
            // Accept username OR email in the same field
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
