package lk.ijse.elite.controller;

import lk.ijse.elite.bo.BOFactory;
import lk.ijse.elite.bo.BOFactory.BOType;
import lk.ijse.elite.bo.custom.AppUserBO;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.Properties;
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
        final String emailInput = txtEmail.getText().trim().toLowerCase();

        if (emailInput.isEmpty()) {
            alert(Alert.AlertType.ERROR, "Please enter your email address.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(emailInput).matches()) {
            alert(Alert.AlertType.ERROR, "Please enter a valid email address.");
            return;
        }

        // Load SMTP creds from environment (avoid hardcoding!)
        final String from = System.getenv("SMTP_FROM");
        final String appPassword = System.getenv("SMTP_APP_PASSWORD");

        if (from == null || from.isBlank() || appPassword == null || appPassword.isBlank()) {
            alert(Alert.AlertType.ERROR,
                    "Email sending is not configured. Please set SMTP_FROM and SMTP_APP_PASSWORD environment variables.");
            return;
        }

        btnRecoverPassword.setDisable(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 1) Check if email exists
                if (!appUserBO.existsByEmail(emailInput)) {
                    throw new IllegalStateException("Email address not found in system.");
                }

                // 2) Generate new password (plain to email), BO will hash it when updating
                String newPassword = appUserBO.generateRandomPassword();

                // 3) Update (hash + save) BEFORE sending email
                boolean updated = appUserBO.updatePassword(emailInput, newPassword);
                if (!updated) {
                    throw new IllegalStateException("Failed to update password in the database.");
                }

                // 4) Send email
                Properties properties = new Properties();
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "587");
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");

                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, appPassword);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailInput));
                message.setSubject("Your New Password for AppliMax");
                message.setText(
                        "As requested, we have generated a new password for your account.\n\n" +
                                "Your new password is: " + newPassword + "\n\n" +
                                "For security, please log in and change it immediately.\n" +
                                "If you did not request this, contact support.\n\n" +
                                "Thank you,\nAppliMax Support Team"
                );

                Transport.send(message);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            btnRecoverPassword.setDisable(false);
            navigateTo("/view/LoginPage.fxml");
            alert(Alert.AlertType.INFORMATION, "Password reset instructions sent to your email.");
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
