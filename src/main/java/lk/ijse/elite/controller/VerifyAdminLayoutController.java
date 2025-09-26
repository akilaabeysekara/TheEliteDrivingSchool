package lk.ijse.elite.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import lk.ijse.elite.bo.BOFactory;
import lk.ijse.elite.bo.custom.AppUserBO;
import lk.ijse.elite.dto.AppUserDTO;
import lk.ijse.elite.security.PasswordUtil;

import java.io.IOException;

public class VerifyAdminLayoutController {

    public AnchorPane ancVeryfyAdmin;
    @FXML private Button loginButton;
    @FXML private PasswordField passwordField;

    private final AppUserBO appUserBO = BOFactory.getInstance().getBO(BOFactory.BOType.USER);

    @FXML
    void onLog(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER -> onLogin(null);
        }
    }

    @FXML
    void onLogin(ActionEvent event) {
        String enteredPassword = passwordField.getText() == null ? "" : passwordField.getText().trim();
        if (enteredPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Login Failed", "Password cannot be empty");
            return;
        }

        try {
            AppUserDTO adminUser = appUserBO.getUserByRole("ADMIN");
            if (adminUser == null) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "No admin user found.");
                return;
            }

            // ✅ compare raw vs hash using BCrypt
            if (PasswordUtil.matches(enteredPassword, adminUser.getPassword())) {
                navigateTo("/view/UserPage.fxml");
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Incorrect password. Please try again.");
                passwordField.clear();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error checking admin credentials");
            e.printStackTrace();
        }
    }

    private void navigateTo(String path) {
        try {
            ancVeryfyAdmin.getChildren().clear();
            AnchorPane anchorPane = FXMLLoader.load(getClass().getResource(path));
            anchorPane.prefWidthProperty().bind(ancVeryfyAdmin.widthProperty());
            anchorPane.prefHeightProperty().bind(ancVeryfyAdmin.heightProperty());
            ancVeryfyAdmin.getChildren().add(anchorPane);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Page not found..!").show();
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
