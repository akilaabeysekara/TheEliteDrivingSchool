package lk.ijse.elite.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import lk.ijse.elite.config.FactoryConfiguration;
import lk.ijse.elite.dto.InstructorDTO;
import lk.ijse.elite.entity.Instructor;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class InstructorPageController implements Initializable {

    public Label lblId;
    public TextField txtName;
    public TextField txtNic;
    public TextField txtEmail;
    public TextField txtPhone;
    public TextField txtAddress;

    public TableView<InstructorDTO> tblInstructor;
    public TableColumn<InstructorDTO, String> colId;
    public TableColumn<InstructorDTO, String> colName;
    public TableColumn<InstructorDTO, String> colNic;
    public TableColumn<InstructorDTO, String> colEmail;
    public TableColumn<InstructorDTO, String> colPhone;
    public TableColumn<InstructorDTO, String> colAddress;

    public Button btnDelete;
    public Button btnUpdate;
    public Button btnSave;

    // Validation (same as Student)
    private final String namePattern  = "^[A-Za-z ]+$";
    private final String nicPattern   = "^(\\d{9}[vVxX])|(\\d{12})$";
    private final String emailPattern = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private final String phonePattern = "^\\d{10}$";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("instructorId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        colNic.setCellValueFactory(new PropertyValueFactory<>("instructorNic"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("instructorEmail"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("instructorPhone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("instructorAddress"));

        try {
            resetPage();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load instructor data.");
        }
    }

    // ---------- UI helpers ----------

    private void resetPage() throws Exception {
        loadNextId();
        loadTableData();
        btnDelete.setDisable(true);
        btnUpdate.setDisable(true);
        btnSave.setDisable(false);
        clearFields();
        resetFieldStyles();
        tblInstructor.getSelectionModel().clearSelection();
    }

    private void clearFields() {
        txtName.clear(); txtNic.clear(); txtEmail.clear(); txtPhone.clear(); txtAddress.clear();
    }

    private void resetFieldStyles() {
        String base = "-fx-background-radius: 3; -fx-background-color: rgba(216,216,255,0.88); -fx-border-width: 0 0 2 0; -fx-border-color: black;";
        txtName.setStyle(base); txtNic.setStyle(base); txtEmail.setStyle(base); txtPhone.setStyle(base); txtAddress.setStyle(base);
    }

    private void markInvalid(TextField field) {
        field.setStyle("-fx-background-radius: 3; -fx-background-color: rgba(216,216,255,0.88); -fx-border-color: red;");
    }

    private void showInfo(String m){ new Alert(Alert.AlertType.INFORMATION, m).show(); }
    private void showError(String m){ new Alert(Alert.AlertType.ERROR, m).show(); }

    // ---------- Hibernate ops ----------

    private void loadTableData() throws Exception {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            List<InstructorDTO> rows = session.createQuery(
                    "select new lk.ijse.elite.dto.InstructorDTO(" +
                            "i.instructorId, i.instructorName, i.instructorNic, i.instructorEmail, i.instructorPhone, i.instructorAddress) " +
                            "from Instructor i order by i.instructorId",
                    InstructorDTO.class
            ).getResultList();
            tblInstructor.setItems(FXCollections.observableArrayList(rows));
        }
    }

    private void loadNextId() throws Exception {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            String last = session.createQuery(
                    "select i.instructorId from Instructor i order by i.instructorId desc",
                    String.class
            ).setMaxResults(1).uniqueResult();
            lblId.setText(nextIdFrom(last));
        }
    }

    private String nextIdFrom(String lastId) {
        if (lastId == null) return "I001";
        String digits = lastId.replaceAll("\\D+", "");
        int n = digits.isEmpty() ? 0 : Integer.parseInt(digits);
        return String.format("I%03d", n + 1);
    }

    private boolean saveInstructor(InstructorDTO dto) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            Instructor i = new Instructor(dto.getInstructorId(), dto.getInstructorName(), dto.getInstructorNic(),
                    dto.getInstructorEmail(), dto.getInstructorPhone(), dto.getInstructorAddress());
            session.persist(i);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    private boolean updateInstructor(InstructorDTO dto) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            Instructor i = session.get(Instructor.class, dto.getInstructorId());
            if (i == null) { tx.rollback(); return false; }
            i.setInstructorName(dto.getInstructorName());
            i.setInstructorNic(dto.getInstructorNic());
            i.setInstructorEmail(dto.getInstructorEmail());
            i.setInstructorPhone(dto.getInstructorPhone());
            i.setInstructorAddress(dto.getInstructorAddress());
            session.merge(i);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    private boolean deleteInstructor(String id) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            Instructor i = session.get(Instructor.class, id);
            if (i == null) { tx.commit(); return false; }
            session.remove(i);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // ---------- Actions ----------

    public void btnInstructorSaveOnAction(ActionEvent event) {
        InstructorDTO dto = extractAndValidateInput();
        if (dto == null) return;
        try {
            if (saveInstructor(dto)) { resetPage(); showInfo("Instructor saved successfully!"); }
            else showError("Failed to save instructor.");
        } catch (Exception e) { e.printStackTrace(); showError("Error while saving instructor.\n" + e.getMessage()); }
    }

    public void btnInstructorUpdateOnAction(ActionEvent event) {
        InstructorDTO dto = extractAndValidateInput();
        if (dto == null) return;
        try {
            if (updateInstructor(dto)) { resetPage(); showInfo("Instructor updated successfully!"); }
            else showError("Instructor not found.");
        } catch (Exception e) { e.printStackTrace(); showError("Error while updating instructor.\n" + e.getMessage()); }
    }

    public void btnInstructorDeleteOnAction(ActionEvent event) {
        String id = lblId.getText();
        if (id == null || id.isBlank()) { showError("Select an instructor first."); return; }

        Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this instructor?", ButtonType.YES, ButtonType.NO).showAndWait();

        if (res.isPresent() && res.get() == ButtonType.YES) {
            try {
                if (deleteInstructor(id)) { resetPage(); showInfo("Instructor deleted successfully."); }
                else showError("Instructor not found.");
            } catch (Exception e) { e.printStackTrace(); showError("Error while deleting instructor.\n" + e.getMessage()); }
        }
    }

    public void btnInstructorResetOnAction(ActionEvent event) {
        try { resetPage(); } catch (Exception e) { e.printStackTrace(); showError("Failed to reset."); }
    }

    public void onClickTable(MouseEvent event) {
        InstructorDTO s = tblInstructor.getSelectionModel().getSelectedItem();
        if (s != null) {
            lblId.setText(s.getInstructorId());
            txtName.setText(s.getInstructorName());
            txtNic.setText(s.getInstructorNic());
            txtEmail.setText(s.getInstructorEmail());
            txtPhone.setText(s.getInstructorPhone());
            txtAddress.setText(s.getInstructorAddress());

            btnSave.setDisable(true);
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
        }
    }

    // ---------- Validation ----------

    private InstructorDTO extractAndValidateInput() {
        String id      = nz(lblId.getText());
        String name    = nz(txtName.getText());
        String nic     = nz(txtNic.getText());
        String email   = nz(txtEmail.getText()).toLowerCase();
        String phone   = nz(txtPhone.getText());
        String address = nz(txtAddress.getText());

        resetFieldStyles();

        boolean valid = true;
        if (!name.matches(namePattern))   { markInvalid(txtName);   valid = false; }
        if (!nic.matches(nicPattern))     { markInvalid(txtNic);    valid = false; }
        if (!email.matches(emailPattern)) { markInvalid(txtEmail);  valid = false; }
        if (!phone.matches(phonePattern)) { markInvalid(txtPhone);  valid = false; }
        if (address.isEmpty())            { markInvalid(txtAddress); valid = false; }

        if (!valid) return null;

        return new InstructorDTO(id, name, nic, email, phone, address);
    }

    private static String nz(String s) { return s == null ? "" : s.trim(); }
}
