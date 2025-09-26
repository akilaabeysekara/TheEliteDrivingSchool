package lk.ijse.elite.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class OpenPageController {

    public static void openPage(AnchorPane currentAnchorPane, String path) {
        try {
            currentAnchorPane.getChildren().clear();
            AnchorPane anchorPane = FXMLLoader.load(OpenPageController.class.getResource(path));
            anchorPane.prefWidthProperty().bind(currentAnchorPane.widthProperty());
            anchorPane.prefHeightProperty().bind(currentAnchorPane.heightProperty());
            currentAnchorPane.getChildren().add(anchorPane);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Page not found..!").show();
            e.printStackTrace();
        }
    }
}