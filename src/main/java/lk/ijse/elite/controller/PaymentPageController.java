package lk.ijse.elite.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

import lk.ijse.elite.bo.BOFactory;
import lk.ijse.elite.bo.custom.PaymentBO;
import lk.ijse.elite.dto.PaymentDTO;
import lk.ijse.elite.entity.Enrollment;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PaymentPageController implements Initializable {

    // form
    public Label lblId;
    public ComboBox<Enrollment> cmbEnrollment;
    public DatePicker dpDate;
    public TextField txtAmount;
    public ComboBox<String> cmbMethod;
    public TextField txtNote;

    // table
    public TableView<PaymentDTO> tblPayment;
    public TableColumn<PaymentDTO, String> colId, colStudent, colCourse, colMethod, colNote;
    public TableColumn<PaymentDTO, LocalDate> colDate;
    public TableColumn<PaymentDTO, BigDecimal> colAmount;

    // buttons
    public Button btnSave, btnUpdate, btnDelete, btnReset;

    // BO
    private final PaymentBO paymentBO = BOFactory.getInstance().getBO(BOFactory.BOType.PAYMENT);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // table bindings
        colId.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("paidDate"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        // enrollment renderer
        cmbEnrollment.setConverter(new StringConverter<>() {
            @Override public String toString(Enrollment e) {
                if (e == null) return "";
                return e.getStudent().getStudentName() + "  |  " + e.getCourse().getCourseName();
            }
            @Override public Enrollment fromString(String s) { return null; }
        });

        // methods
        cmbMethod.setItems(FXCollections.observableArrayList("CASH", "CARD", "ONLINE", "OTHER"));

        try {
            loadEnrollments();
            resetPage();
        } catch (Exception e) {
            e.printStackTrace();
            error("Failed to initialize payment page.");
        }
    }

    // ==== helpers ====
    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m).show(); }
    private void error(String m){ new Alert(Alert.AlertType.ERROR, m).show(); }

    private void resetPage() throws Exception {
        loadNextId();
        loadTableData();

        dpDate.setValue(null);
        txtAmount.clear();
        txtNote.clear();
        cmbMethod.getSelectionModel().clearSelection();
        cmbEnrollment.getSelectionModel().clearSelection();

        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        tblPayment.getSelectionModel().clearSelection();
    }

    private void loadEnrollments() throws Exception {
        List<Enrollment> list = paymentBO.listEnrollmentsForCombo();
        cmbEnrollment.setItems(FXCollections.observableArrayList(list));
    }

    private void loadTableData() throws Exception {
        tblPayment.setItems(FXCollections.observableArrayList(paymentBO.getAllPayments()));
    }

    private void loadNextId() throws Exception {
        lblId.setText(paymentBO.getNextId());
    }

    // ==== actions (FXML) ====
    public void btnPaymentSaveOnAction(ActionEvent e) {
        PaymentDTO dto = extract();
        if (dto == null) return;

        Enrollment selected = cmbEnrollment.getValue();
        if (selected == null) { error("Select an enrollment."); return; }

        try {
            paymentBO.save(dto, selected.getEnrollmentId());
            info("Payment saved.");
            resetPage();
        } catch (Exception ex) { ex.printStackTrace(); error("Error saving payment.\n" + ex.getMessage()); }
    }

    public void btnPaymentUpdateOnAction(ActionEvent e) {
        PaymentDTO dto = extract();
        if (dto == null) return;

        Enrollment selected = cmbEnrollment.getValue();
        if (selected == null) { error("Select an enrollment."); return; }

        try {
            paymentBO.update(dto, selected.getEnrollmentId());
            info("Payment updated.");
            resetPage();
        } catch (Exception ex) { ex.printStackTrace(); error("Error updating payment.\n" + ex.getMessage()); }
    }

    public void btnPaymentDeleteOnAction(ActionEvent e) {
        Optional<ButtonType> r = new Alert(
                Alert.AlertType.CONFIRMATION, "Delete this payment?", ButtonType.YES, ButtonType.NO
        ).showAndWait();

        if (r.isPresent() && r.get() == ButtonType.YES) {
            try {
                boolean ok = paymentBO.delete(lblId.getText());
                if (ok) { info("Payment deleted."); resetPage(); }
                else error("Payment not found.");
            } catch (Exception ex) { ex.printStackTrace(); error("Error deleting payment.\n" + ex.getMessage()); }
        }
    }

    public void btnPaymentResetOnAction(ActionEvent e) {
        try { resetPage(); } catch (Exception ex) { ex.printStackTrace(); error("Failed to reset."); }
    }

    public void onClickTable(MouseEvent e) {
        PaymentDTO d = tblPayment.getSelectionModel().getSelectedItem();
        if (d == null) return;

        lblId.setText(d.getPaymentId());

        // choose matching enrollment by student & course
        cmbEnrollment.getItems().stream()
                .filter(en -> en.getStudent().getStudentId().equals(d.getStudentId())
                        && en.getCourse().getCourseId().equals(d.getCourseId()))
                .findFirst()
                .ifPresent(cmbEnrollment::setValue);

        dpDate.setValue(d.getPaidDate());
        txtAmount.setText(d.getAmount() == null ? "" : d.getAmount().toPlainString());
        cmbMethod.setValue(d.getMethod());
        txtNote.setText(d.getNote());

        btnSave.setDisable(true);
        btnUpdate.setDisable(false);
        btnDelete.setDisable(false);
    }

    // ==== extract & validate ====
    private PaymentDTO extract() {
        String id = nz(lblId.getText());
        Enrollment en = cmbEnrollment.getValue();
        LocalDate date = dpDate.getValue();
        String amt = nz(txtAmount.getText());
        String method = nz(cmbMethod.getValue());
        String note = nz(txtNote.getText());

        if (id.isBlank() || en == null || date == null || amt.isBlank() || method.isBlank()) {
            error("Please fill all required fields.");
            return null;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amt);
            if (amount.signum() <= 0) throw new NumberFormatException();
        } catch (Exception ex) {
            error("Amount must be a positive number.");
            return null;
        }

        return new PaymentDTO(
                id,
                en.getStudent().getStudentId(), en.getStudent().getStudentName(),
                en.getCourse().getCourseId(),    en.getCourse().getCourseName(),
                date, amount, method, note
        );
    }

    private static String nz(String s){ return s == null ? "" : s.trim(); }
}
