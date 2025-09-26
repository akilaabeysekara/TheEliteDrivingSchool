package lk.ijse.elite.controller;

import lk.ijse.elite.bo.BOFactory;
import lk.ijse.elite.bo.BOFactory.BOType;
import lk.ijse.elite.bo.custom.AppUserBO;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.regex.Pattern;

public class ForgotPasswordController {

    @FXML private AnchorPane ancMainContainer;
    @FXML private Button btnRecoverPassword;
    @FXML private ImageView iconBack;
    @FXML private TextField txtEmail;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final AppUserBO appUserBO = BOFactory.getInstance().getBO(BOType.USER);

    @FXML
    void btnRecoverPasswordOnAction(ActionEvent event) {
        final String emailInput = (txtEmail.getText() == null ? "" : txtEmail.getText().trim().toLowerCase());

        if (emailInput.isEmpty()) {
            alert(Alert.AlertType.ERROR, "Please enter your email address.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(emailInput).matches()) {
            alert(Alert.AlertType.ERROR, "Please enter a valid email address.");
            return;
        }

        btnRecoverPassword.setDisable(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // BO does: verify, generate, update (hash), and send email
                appUserBO.resetPasswordAndEmail(emailInput);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            btnRecoverPassword.setDisable(false);
            navigateTo("/view/LoginPage.fxml");
            alert(Alert.AlertType.INFORMATION, "Password reset email has been sent.");
        });

        task.setOnFailed(e -> {
            btnRecoverPassword.setDisable(false);
            Throwable ex = task.getException();
            String msg = (ex != null && ex.getMessage() != null) ? ex.getMessage() : "Unknown error.";
            alert(Alert.AlertType.ERROR, "Failed to reset password: " + msg);
        });

        new Thread(task, "forgot-password-task").start();
    }

    @FXML
    void onClickedBack(MouseEvent event) {
        navigateTo("/view/LoginPage.fxml");
    }

    @FXML
    void onNext(KeyEvent event) {
        if ("ENTER".equals(event.getCode().toString())) {
            btnRecoverPassword.fire();
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
            alert(Alert.AlertType.ERROR, "Page not found..!");
            e.printStackTrace();
        }
    }

    private void alert(Alert.AlertType type, String content) {
        new Alert(type, content).show();
    }
}
