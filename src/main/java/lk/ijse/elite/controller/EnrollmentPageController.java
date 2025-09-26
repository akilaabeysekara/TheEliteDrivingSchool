package lk.ijse.elite.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

import lk.ijse.elite.bo.BOFactory;
import lk.ijse.elite.bo.custom.EnrollmentBO;
import lk.ijse.elite.config.FactoryConfiguration;
import lk.ijse.elite.dto.EnrollmentDTO;
import lk.ijse.elite.entity.Course;
import lk.ijse.elite.entity.Student;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EnrollmentPageController implements Initializable {

    // ===== form =====
    public Label lblEnrollmentId;     // << changed
    public ComboBox<Student> cmbStudent;
    public ComboBox<Course>  cmbCourse;
    public DatePicker dpRegDate;
    public TextField txtUpfront;
    public ComboBox<String> cmbStatus;

    // ===== table =====
    public TableView<EnrollmentDTO> tblEnrollment;
    public TableColumn<EnrollmentDTO, String> colEnrollId, colStudentId, colStudentName, colCourseId, colCourseName, colStatus;
    public TableColumn<EnrollmentDTO, LocalDate> colRegDate;
    public TableColumn<EnrollmentDTO, BigDecimal> colUpfront;

    // ===== buttons =====
    public Button btnSave, btnUpdate, btnDelete, btnReset;

    private final EnrollmentBO enrollmentBO = BOFactory.getInstance().getBO(BOFactory.BOType.ENROLLMENT);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // table bindings
        colEnrollId.setCellValueFactory(new PropertyValueFactory<>("enrollmentId"));
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colRegDate.setCellValueFactory(new PropertyValueFactory<>("regDate"));
        colUpfront.setCellValueFactory(new PropertyValueFactory<>("upfrontAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // combos
        cmbStudent.setConverter(new StringConverter<>() {
            @Override public String toString(Student s){ return s == null ? "" : s.getStudentId()+" - "+s.getStudentName(); }
            @Override public Student fromString(String s){ return null; }
        });
        cmbCourse.setConverter(new StringConverter<>() {
            @Override public String toString(Course c){ return c == null ? "" : c.getCourseId()+" - "+c.getCourseName(); }
            @Override public Course fromString(String s){ return null; }
        });
        cmbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "COMPLETED", "CANCELLED"));

        try {
            loadStudents();     // still direct fetch; could be StudentBO if you have one
            loadCourses();      // still direct fetch; could be CourseBO if you have one
            resetPage();
        } catch (Exception e) {
            e.printStackTrace();
            error("Failed to initialize enrollment page.");
        }
    }

    // ===== helpers =====
    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m).show(); }
    private void error(String m){ new Alert(Alert.AlertType.ERROR, m).show(); }

    private void resetPage() throws Exception {
        lblEnrollmentId.setText(enrollmentBO.getNextId());
        loadTableData();

        cmbStudent.getSelectionModel().clearSelection();
        cmbCourse.getSelectionModel().clearSelection();
        dpRegDate.setValue(LocalDate.now());
        txtUpfront.clear();
        cmbStatus.getSelectionModel().select("ACTIVE");

        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        tblEnrollment.getSelectionModel().clearSelection();
    }

    // ===== fetch reference data (can be moved to BOs later) =====
    private void loadStudents() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            List<Student> list = s.createQuery("from Student st order by st.studentId", Student.class).getResultList();
            cmbStudent.setItems(FXCollections.observableArrayList(list));
        }
    }

    private void loadCourses() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            List<Course> list = s.createQuery("from Course c order by c.courseId", Course.class).getResultList();
            cmbCourse.setItems(FXCollections.observableArrayList(list));
        }
    }

    private void loadTableData() throws Exception {
        tblEnrollment.setItems(FXCollections.observableArrayList(enrollmentBO.getAll()));
    }

    // ===== Actions =====
    public void btnEnrollmentSaveOnAction(ActionEvent ev) {
        EnrollmentDTO dto = extract();
        if (dto == null) return;
        try {
            dto.setEnrollmentId(lblEnrollmentId.getText());
            if (enrollmentBO.save(dto)) {
                info("Enrollment saved.");
                resetPage();
            } else error("Failed to save enrollment.");
        } catch (Exception e) {
            e.printStackTrace();
            error("Error saving enrollment.\n" + e.getMessage());
        }
    }

    public void btnEnrollmentUpdateOnAction(ActionEvent ev) {
        EnrollmentDTO dto = extract();
        if (dto == null) return;
        try {
            dto.setEnrollmentId(lblEnrollmentId.getText());
            if (enrollmentBO.update(dto)) {
                info("Enrollment updated.");
                resetPage();
            } else error("Enrollment not found.");
        } catch (Exception e) {
            e.printStackTrace();
            error("Error updating enrollment.\n" + e.getMessage());
        }
    }

    public void btnEnrollmentDeleteOnAction(ActionEvent ev) {
        String id = lblEnrollmentId.getText();
        if (id == null || id.isBlank()) { error("Select an enrollment first."); return; }

        Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this enrollment?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            try {
                if (enrollmentBO.delete(id)) {
                    info("Enrollment deleted.");
                    resetPage();
                } else error("Enrollment not found.");
            } catch (Exception e) {
                e.printStackTrace();
                error("Error deleting enrollment.\n" + e.getMessage());
            }
        }
    }

    public void btnEnrollmentResetOnAction(ActionEvent ev) {
        try { resetPage(); } catch (Exception e) { e.printStackTrace(); error("Failed to reset."); }
    }

    public void onClickTable(MouseEvent e) {
        EnrollmentDTO d = tblEnrollment.getSelectionModel().getSelectedItem();
        if (d == null) return;

        lblEnrollmentId.setText(d.getEnrollmentId());

        cmbStudent.getItems().stream()
                .filter(s -> s.getStudentId().equals(d.getStudentId()))
                .findFirst().ifPresent(cmbStudent::setValue);

        cmbCourse.getItems().stream()
                .filter(c -> c.getCourseId().equals(d.getCourseId()))
                .findFirst().ifPresent(cmbCourse::setValue);

        dpRegDate.setValue(d.getRegDate());
        txtUpfront.setText(d.getUpfrontAmount() == null ? "" : d.getUpfrontAmount().toPlainString());
        cmbStatus.setValue(d.getStatus());

        btnSave.setDisable(true);
        btnUpdate.setDisable(false);
        btnDelete.setDisable(false);
    }

    // ===== validation & extract =====
    private EnrollmentDTO extract() {
        Student st = cmbStudent.getValue();
        Course  c  = cmbCourse.getValue();
        LocalDate date = dpRegDate.getValue();
        String upfront = nz(txtUpfront.getText());
        String status  = nz(cmbStatus.getValue());

        if (st == null || c == null || date == null || upfront.isBlank() || status.isBlank()) {
            error("Please fill all required fields.");
            return null;
        }

        java.math.BigDecimal amount;
        try {
            amount = new java.math.BigDecimal(upfront);
            if (amount.signum() < 0) throw new NumberFormatException();
        } catch (Exception ex) {
            error("Upfront amount must be a non-negative number.");
            return null;
        }

        return new EnrollmentDTO(
                null, // enrollmentId will be set by lblEnrollmentId or BO
                st.getStudentId(), st.getStudentName(),
                c.getCourseId(),  c.getCourseName(),
                date, amount, status
        );
    }

    private static String nz(String s){ return s == null ? "" : s.trim(); }
}
