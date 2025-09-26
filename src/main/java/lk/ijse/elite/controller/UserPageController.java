package lk.ijse.elite.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import lk.ijse.elite.bo.BOFactory;
import lk.ijse.elite.bo.BOFactory.BOType;
import lk.ijse.elite.bo.custom.AppUserBO;
import lk.ijse.elite.dto.AppUserDTO;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserPageController implements Initializable {

    @FXML private TableView<AppUserDTO> tblUser;
    @FXML private TableColumn<AppUserDTO, String> colId;
    @FXML private TableColumn<AppUserDTO, String> colName;
    @FXML private TableColumn<AppUserDTO, String> colEmail;
    @FXML private TableColumn<AppUserDTO, String> colPassword;
    @FXML private TableColumn<AppUserDTO, String> colRole;

    @FXML private Button btnDelete, btnReset, btnSave, btnUpdate;

    @FXML private Label  lblId;
    @FXML private TextField txtUserName, txtUserPassword, txtEmail;
    @FXML private ComboBox<String> cmbRole;

    private final AppUserBO appUserBO = BOFactory.getInstance().getBO(BOType.USER);

    private static final String NAME_PATTERN     = "^[A-Za-z0-9._-]{3,40}$";
    private static final String PASSWORD_PATTERN = "^.{4,64}$";
    private static final String EMAIL_PATTERN    = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("userRole"));
        colPassword.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : "••••••");
            }
        });

        // roles
        cmbRole.setItems(FXCollections.observableArrayList("ADMIN", "RECEPTIONIST"));
        try {
            resetPage();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load users.");
        }
    }

    private void resetPage() throws Exception {
        loadNextId();
        loadTableData();
        clearFields();
        tblUser.getSelectionModel().clearSelection();
        btnDelete.setDisable(true);
        btnUpdate.setDisable(true);
        btnSave.setDisable(false);
    }

    private void loadNextId() throws Exception {
        String nextId = appUserBO.getNextId();
        lblId.setText(nextId == null ? "" : nextId);
    }

    private void loadTableData() throws Exception {
        tblUser.setItems(FXCollections.observableArrayList(appUserBO.getAllUsers()));
    }

    private void clearFields() {
        txtUserName.clear();
        txtUserPassword.clear();
        txtEmail.clear();
        cmbRole.getSelectionModel().select("USER");
        txtUserName.setStyle(""); txtUserPassword.setStyle(""); txtEmail.setStyle("");
        cmbRole.setStyle("");
    }

    @FXML
    void btnUserSaveOnAction(ActionEvent event) {
        String userId   = nz(lblId.getText());
        String userName = nz(txtUserName.getText());
        String password = nz(txtUserPassword.getText());
        String email    = nz(txtEmail.getText()).trim().toLowerCase();
        String userRole = getRole();

        if (userId.isBlank() || userName.isBlank() || password.isBlank() || email.isBlank()) {
            showError("Please fill in ID, Username, Password, and Email.");
            return;
        }

        boolean okName  = userName.matches(NAME_PATTERN);
        boolean okPwd   = password.matches(PASSWORD_PATTERN);
        boolean okEmail = email.matches(EMAIL_PATTERN);
        boolean okRole  = !userRole.isBlank();

        styleInvalid(txtUserName, !okName);
        styleInvalid(txtUserPassword, !okPwd);
        styleInvalid(txtEmail, !okEmail);
        styleInvalid(cmbRole, !okRole);

        if (!okName || !okPwd || !okEmail || !okRole) {
            showError("Please fix invalid fields.");
            return;
        }

        AppUserDTO dto = new AppUserDTO(userId, userName, password, email, userRole);
        try {
            appUserBO.saveUser(dto); // hashes inside BO
            resetPage();
            showInfo("User saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error occurred while saving user.");
        }
    }

    @FXML
    void btnUserUpdateOnAction(ActionEvent event) {
        String userId   = nz(lblId.getText());
        String userName = nz(txtUserName.getText());
        String password = nz(txtUserPassword.getText()); // may be blank to keep existing hash
        String email    = nz(txtEmail.getText()).trim().toLowerCase();
        String userRole = getRole();

        if (userId.isBlank()) { showError("Select a user first."); return; }

        boolean okName  = !userName.isBlank() && userName.matches(NAME_PATTERN);
        boolean okPwd   = password.isBlank() || password.matches(PASSWORD_PATTERN); // allow blank to keep old
        boolean okEmail = !email.isBlank() && email.matches(EMAIL_PATTERN);
        boolean okRole  = !userRole.isBlank();

        styleInvalid(txtUserName, !okName);
        styleInvalid(txtUserPassword, !okPwd && !password.isBlank());
        styleInvalid(txtEmail, !okEmail);
        styleInvalid(cmbRole, !okRole);

        if (!okName || !okPwd || !okEmail || !okRole) {
            showError("Please fill required/valid fields.");
            return;
        }

        AppUserDTO dto = new AppUserDTO(userId, userName, password, email, userRole);
        try {
            appUserBO.updateUser(dto); // hashes inside if password provided
            resetPage();
            showInfo("User updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error occurred while updating user.");
        }
    }

    @FXML
    void btnUserDeleteOnAction(ActionEvent event) {
        String id = nz(lblId.getText());
        if (id.isBlank()) { showError("Select a user first."); return; }

        Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this user?", ButtonType.YES, ButtonType.NO).showAndWait();

        if (res.isPresent() && res.get() == ButtonType.YES) {
            try {
                boolean deleted = appUserBO.deleteUser(id);
                if (deleted) { resetPage(); showInfo("User deleted successfully."); }
                else showError("Cannot delete user. It may be referenced elsewhere.");
            } catch (Exception e) { e.printStackTrace(); showError("Error occurred while deleting user."); }
        }
    }

    @FXML
    void btnUserResetOnAction(ActionEvent event) {
        try { resetPage(); } catch (Exception e) { e.printStackTrace(); showError("Failed to reset."); }
    }

    @FXML
    void onClickTable(MouseEvent event) {
        AppUserDTO s = tblUser.getSelectionModel().getSelectedItem();
        if (s != null) {
            lblId.setText(nz(s.getUserId()));
            txtUserName.setText(nz(s.getUserName()));
            txtEmail.setText(nz(s.getEmail()));
            cmbRole.getSelectionModel().select(nz(s.getUserRole()).isBlank() ? "USER" : s.getUserRole());
            txtUserPassword.clear(); // never show stored (hashed) password

            btnSave.setDisable(true);
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
        }
    }

    // helpers
    private static String nz(String s) { return s == null ? "" : s; }
    private static void styleInvalid(Control c, boolean bad){ c.setStyle(bad ? "-fx-border-color: red;" : ""); }
    private String getRole() {
        String r = cmbRole.getSelectionModel().getSelectedItem();
        return r == null ? "" : r.trim().toUpperCase();
    }
    private void showInfo(String m){ new Alert(Alert.AlertType.INFORMATION, m).show(); }
    private void showError(String m){ new Alert(Alert.AlertType.ERROR, m).show(); }
}
