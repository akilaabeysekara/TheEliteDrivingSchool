package lk.ijse.elite.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import lk.ijse.elite.security.SessionContext;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DashboardController {

    private static final String ACTIVE_STYLE = "-fx-background-color: #000032; -fx-text-fill: #FFC312; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: #FFC312;";
    private static final String DEFAULT_STYLE = "-fx-background-color: #000032; -fx-text-fill: #74b9ff;";

    @FXML
    private Label lblDate;
    @FXML
    private AnchorPane ancMainContainer;
    @FXML
    private AnchorPane ancMainContainerPlus;

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnStudents;
    @FXML
    private Button btnCourses;
    @FXML
    private Button btnLessons;
    @FXML
    private Button btnInstructors;
    @FXML
    private Button btnEnrollments;
    @FXML
    private Button btnPayments;
    @FXML
    private Button btnUser;
    @FXML
    private Button btnLogout;

    @FXML
    void btnDashboardOnAction(ActionEvent event) {
        navigateTo("/view/OpenPage.fxml");
        resetButtons();
        btnDashboard.setStyle(ACTIVE_STYLE);
    }

    @FXML
    void btnStudentsOnAction(ActionEvent event) {
        navigateTo("/view/StudentPage.fxml");
        resetButtons();
        btnStudents.setStyle(ACTIVE_STYLE);
    }

    @FXML
    void btnCoursesOnAction(ActionEvent event) {
        navigateTo("/view/CoursesPage.fxml");
        resetButtons();
        btnCourses.setStyle(ACTIVE_STYLE);
    }

    @FXML
    void btnLessonsOnAction(ActionEvent event) {
        navigateTo("/view/LessonsPage.fxml");
        resetButtons();
        btnLessons.setStyle(ACTIVE_STYLE);
    }

    @FXML
    void btnInstructorsOnAction(ActionEvent event) {
        navigateTo("/view/InstructorPage.fxml");
        resetButtons();
        btnInstructors.setStyle(ACTIVE_STYLE);
    }

    @FXML
    void btnEnrollmentsOnAction(ActionEvent event) {
        navigateTo("/view/EnrollmentPage.fxml");
        resetButtons();
        btnEnrollments.setStyle(ACTIVE_STYLE);
    }

    @FXML
    void btnPaymentsOnAction(ActionEvent event) {
        navigateTo("/view/PaymentPage.fxml");
        resetButtons();
        btnPayments.setStyle(ACTIVE_STYLE);
    }

    @FXML
    void btnUserOnAction(ActionEvent event) {
        navigateTo("/view/VerifyAdminLayout.fxml");
        resetButtons();
        btnUser.setStyle(ACTIVE_STYLE);
    }

    @FXML
    void btnLogoutOnAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to Logout ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> response = alert.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.YES) {
            navigateToLogout("/view/LoginPage.fxml");
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

    private void navigateToLogout(String path) {
        try {
            ancMainContainerPlus.getChildren().clear();
            AnchorPane anchorPane = FXMLLoader.load(getClass().getResource(path));
            anchorPane.prefWidthProperty().bind(ancMainContainerPlus.widthProperty());
            anchorPane.prefHeightProperty().bind(ancMainContainerPlus.heightProperty());
            ancMainContainerPlus.getChildren().add(anchorPane);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Page not found..!").show();
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        navigateTo("/view/OpenPage.fxml");
        resetButtons();
        btnDashboard.setStyle(ACTIVE_STYLE);
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MMM/yyyy")));

    }

    private void resetButtons() {
        btnDashboard.setStyle(DEFAULT_STYLE);
        btnStudents.setStyle(DEFAULT_STYLE);
        btnCourses.setStyle(DEFAULT_STYLE);
        btnLessons.setStyle(DEFAULT_STYLE);
        btnInstructors.setStyle(DEFAULT_STYLE);
        btnEnrollments.setStyle(DEFAULT_STYLE);
        btnPayments.setStyle(DEFAULT_STYLE);
        btnUser.setStyle(DEFAULT_STYLE);
    }
}