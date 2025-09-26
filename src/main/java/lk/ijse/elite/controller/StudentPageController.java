package lk.ijse.elite.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import lk.ijse.elite.config.FactoryConfiguration;
import lk.ijse.elite.dto.StudentDTO;
import lk.ijse.elite.entity.Student;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StudentPageController implements Initializable {

    public Label lblId;
    public TextField txtName;
    public TextField txtNic;
    public TextField txtEmail;
    public TextField txtPhone;
    public TextField txtAddress;

    public TableView<StudentDTO> tblCustomer;
    public TableColumn<StudentDTO, String> colId;
    public TableColumn<StudentDTO, String> colName;
    public TableColumn<StudentDTO, String> colNic;
    public TableColumn<StudentDTO, String> colEmail;
    public TableColumn<StudentDTO, String> colPhone;
    public TableColumn<StudentDTO, String> colAddress;

    public Button btnDelete;
    public Button btnUpdate;
    public Button btnSave;
    public Button btnReset;

    // --- Validation patterns ---
    private final String namePattern  = "^[A-Za-z ]+$";
    private final String nicPattern   = "^(\\d{9}[vVxX])|(\\d{12})$";
    private final String emailPattern = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private final String phonePattern = "^\\d{10}$";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // table bindings
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colNic.setCellValueFactory(new PropertyValueFactory<>("studentNic"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("studentEmail"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("studentPhone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("studentAddress"));

        try {
            resetPage();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load student data.");
        }
    }

    // ================= UI helpers =================

    private void resetPage() throws Exception {
        loadNextId();
        loadTableData();

        btnDelete.setDisable(true);
        btnUpdate.setDisable(true);
        btnSave.setDisable(false);

        clearFields();
        resetFieldStyles();
        tblCustomer.getSelectionModel().clearSelection();
    }

    private void clearFields() {
        txtName.clear();
        txtNic.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtAddress.clear();
    }

    private void resetFieldStyles() {
        String base = "-fx-background-radius: 3; -fx-background-color: rgba(216,216,255,0.88); -fx-border-width: 0 0 2 0; -fx-border-color: black;";
        txtName.setStyle(base);
        txtNic.setStyle(base);
        txtEmail.setStyle(base);
        txtPhone.setStyle(base);
        txtAddress.setStyle(base);
    }

    private void markInvalid(TextField field) {
        field.setStyle("-fx-background-radius: 3; -fx-background-color: rgba(216,216,255,0.88); -fx-border-color: red;");
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).show();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).show();
    }

    // ================= Hibernate ops =================

    private void loadTableData() throws Exception {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            List<StudentDTO> rows = session.createQuery(
                    "select new lk.ijse.elite.dto.StudentDTO(" +
                            "s.studentId, s.studentName, s.studentNic, s.studentEmail, s.studentPhone, s.studentAddress) " +
                            "from Student s order by s.studentId",
                    StudentDTO.class
            ).getResultList();
            tblCustomer.setItems(FXCollections.observableArrayList(rows));
        }
    }

    private void loadNextId() throws Exception {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            String last = session.createQuery(
                    "select s.studentId from Student s order by s.studentId desc",
                    String.class
            ).setMaxResults(1).uniqueResult();

            lblId.setText(nextIdFrom(last));
        }
    }

    private String nextIdFrom(String lastId) {
        if (lastId == null) return "S001";
        String digits = lastId.replaceAll("\\D+", "");
        int n = digits.isEmpty() ? 0 : Integer.parseInt(digits);
        return String.format("S%03d", n + 1);
    }

    private boolean saveStudent(StudentDTO dto) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            Student s = new Student(
                    dto.getStudentId(),
                    dto.getStudentName(),
                    dto.getStudentNic(),
                    dto.getStudentEmail(),
                    dto.getStudentPhone(),
                    dto.getStudentAddress()
            );
            session.persist(s);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    private boolean updateStudent(StudentDTO dto) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            Student s = session.get(Student.class, dto.getStudentId());
            if (s == null) {
                tx.rollback();
                return false;
            }
            s.setStudentName(dto.getStudentName());
            s.setStudentNic(dto.getStudentNic());
            s.setStudentEmail(dto.getStudentEmail());
            s.setStudentPhone(dto.getStudentPhone());
            s.setStudentAddress(dto.getStudentAddress());
            session.merge(s);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    private boolean deleteStudent(String id) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            Student s = session.get(Student.class, id);
            if (s == null) {
                tx.commit();
                return false;
            }
            session.remove(s);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // ================= Actions (match FXML) =================

    public void btnStudentSaveOnAction(ActionEvent event) {
        StudentDTO dto = extractAndValidateInput();
        if (dto == null) return;

        try {
            if (saveStudent(dto)) {
                resetPage();
                showInfo("Student saved successfully!");
            } else {
                showError("Failed to save student.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error occurred while saving student.\n" + e.getMessage());
        }
    }

    public void btnStudentUpdateOnAction(ActionEvent event) {
        StudentDTO dto = extractAndValidateInput();
        if (dto == null) return;

        try {
            if (updateStudent(dto)) {
                resetPage();
                showInfo("Student updated successfully!");
            } else {
                showError("Student not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error occurred while updating student.\n" + e.getMessage());
        }
    }

    public void btnStudentDeleteOnAction(ActionEvent event) {
        Optional<ButtonType> result = new Alert(
                Alert.AlertType.CONFIRMATION, "Are you sure?", ButtonType.YES, ButtonType.NO
        ).showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                if (deleteStudent(lblId.getText())) {
                    resetPage();
                    showInfo("Student deleted successfully.");
                } else {
                    showError("Student not found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error occurred while deleting student.\n" + e.getMessage());
            }
        }
    }

    public void btnStudentResetOnAction(ActionEvent event) {
        try {
            resetPage();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to reset.");
        }
    }

    public void onClickTable(MouseEvent event) {
        StudentDTO selected = tblCustomer.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lblId.setText(selected.getStudentId());
            txtName.setText(selected.getStudentName());
            txtNic.setText(selected.getStudentNic());
            txtEmail.setText(selected.getStudentEmail());
            txtPhone.setText(selected.getStudentPhone());
            txtAddress.setText(selected.getStudentAddress());

            btnSave.setDisable(true);
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
        }
    }

    // ================= Validation =================

    private StudentDTO extractAndValidateInput() {
        String id      = nz(lblId.getText());
        String name    = nz(txtName.getText());
        String nic     = nz(txtNic.getText());
        String email   = nz(txtEmail.getText()).toLowerCase();
        String phone   = nz(txtPhone.getText());
        String address = nz(txtAddress.getText());

        resetFieldStyles();

        boolean valid = true;
        if (!name.matches(namePattern))   { markInvalid(txtName);    valid = false; }
        if (!nic.matches(nicPattern))     { markInvalid(txtNic);     valid = false; }
        if (!email.matches(emailPattern)) { markInvalid(txtEmail);   valid = false; }
        if (!phone.matches(phonePattern)) { markInvalid(txtPhone);   valid = false; }
        if (address.isEmpty())            { markInvalid(txtAddress); valid = false; }

        if (!valid) return null;

        return new StudentDTO(id, name, nic, email, phone, address);
    }

    private static String nz(String s) { return s == null ? "" : s.trim(); }
}
