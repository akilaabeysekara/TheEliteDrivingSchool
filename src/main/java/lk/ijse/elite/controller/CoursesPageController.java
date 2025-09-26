package lk.ijse.elite.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import lk.ijse.elite.config.FactoryConfiguration;
import lk.ijse.elite.dto.CourseDTO;
import lk.ijse.elite.entity.Course;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CoursesPageController implements Initializable {

    public Label lblId;
    public TextField txtName;
    public TextField txtDuration;
    public TextField txtFee;

    public TableView<CourseDTO> tblCourse;
    public TableColumn<CourseDTO, String> colId;
    public TableColumn<CourseDTO, String> colName;
    public TableColumn<CourseDTO, String> colDuration;
    public TableColumn<CourseDTO, String> colFee;

    public Button btnDelete;
    public Button btnUpdate;
    public Button btnSave;

    private static final String NAME_PATTERN     = "^[A-Za-z0-9 .&()/-]{3,60}$";
    private static final String DURATION_PATTERN = "^[A-Za-z0-9 ]{2,20}$";           // e.g. 12 weeks / 3 months
    private static final String FEE_PATTERN      = "^\\d{1,9}(\\.\\d{1,2})?$";       // up to 2 decimals

    private final DecimalFormat feeFormat = new DecimalFormat("#,##0.00");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colFee.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getFee() == null ? "" : feeFormat.format(cd.getValue().getFee()))
        );

        try {
            resetPage();
            // Optional: preload sample data once
            // seedDefaultsIfEmpty();
            // loadTableData();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load course data.");
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
        tblCourse.getSelectionModel().clearSelection();
    }

    private void clearFields() {
        txtName.clear();
        txtDuration.clear();
        txtFee.clear();
    }

    private void resetFieldStyles() {
        String base = "-fx-background-radius: 3; -fx-background-color: rgba(216,216,255,0.88); -fx-border-width: 0 0 2 0; -fx-border-color: black;";
        txtName.setStyle(base);
        txtDuration.setStyle(base);
        txtFee.setStyle(base);
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

    // ---------- Hibernate ops ----------

    private void loadTableData() throws Exception {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            List<CourseDTO> rows = session.createQuery(
                    "select new lk.ijse.elite.dto.CourseDTO(" +
                            "c.courseId, c.courseName, c.duration, c.fee) " +
                            "from Course c order by c.courseId",
                    CourseDTO.class
            ).getResultList();
            tblCourse.setItems(FXCollections.observableArrayList(rows));
        }
    }

    private void loadNextId() throws Exception {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            String last = session.createQuery(
                    "select c.courseId from Course c order by c.courseId desc",
                    String.class
            ).setMaxResults(1).uniqueResult();
            lblId.setText(nextIdFrom(last));
        }
    }

    // If no rows yet, start at C1001 (matches your sample IDs)
    private String nextIdFrom(String lastId) {
        if (lastId == null) return "C1001";
        String digits = lastId.replaceAll("\\D+", "");
        int n = digits.isEmpty() ? 1000 : Integer.parseInt(digits);
        return String.format("C%d", n + 1);
    }

    private boolean saveCourse(CourseDTO dto) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            Course c = new Course(dto.getCourseId(), dto.getCourseName(), dto.getDuration(), dto.getFee());
            session.persist(c);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    private boolean updateCourse(CourseDTO dto) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            Course c = session.get(Course.class, dto.getCourseId());
            if (c == null) {
                tx.rollback();
                return false;
            }
            c.setCourseName(dto.getCourseName());
            c.setDuration(dto.getDuration());
            c.setFee(dto.getFee());
            session.merge(c);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    private boolean deleteCourse(String id) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            Course c = session.get(Course.class, id);
            if (c == null) {
                tx.commit();
                return false;
            }
            session.remove(c);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // ---------- Actions ----------

    public void btnCourseSaveOnAction(ActionEvent event) {
        CourseDTO dto = extractAndValidateInput();
        if (dto == null) return;

        try {
            if (saveCourse(dto)) {
                resetPage();
                showInfo("Course saved successfully!");
            } else {
                showError("Failed to save course.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error occurred while saving course.\n" + e.getMessage());
        }
    }

    public void btnCourseUpdateOnAction(ActionEvent event) {
        CourseDTO dto = extractAndValidateInput();
        if (dto == null) return;

        try {
            if (updateCourse(dto)) {
                resetPage();
                showInfo("Course updated successfully!");
            } else {
                showError("Course not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error occurred while updating course.\n" + e.getMessage());
        }
    }

    public void btnCourseDeleteOnAction(ActionEvent event) {
        String id = lblId.getText();
        if (id == null || id.isBlank()) {
            showError("Select a course first.");
            return;
        }

        Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this course?",
                ButtonType.YES, ButtonType.NO).showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                if (deleteCourse(id)) {
                    resetPage();
                    showInfo("Course deleted successfully.");
                } else {
                    showError("Course not found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error occurred while deleting course.\n" + e.getMessage());
            }
        }
    }

    public void btnCourseResetOnAction(ActionEvent event) {
        try {
            resetPage();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to reset.");
        }
    }

    public void onClickTable(MouseEvent event) {
        CourseDTO selected = tblCourse.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lblId.setText(selected.getCourseId());
            txtName.setText(selected.getCourseName());
            txtDuration.setText(selected.getDuration());
            txtFee.setText(selected.getFee() == null ? "" : selected.getFee().toPlainString());

            btnSave.setDisable(true);
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
        }
    }

    // ---------- Validation & DTO mapping ----------

    private CourseDTO extractAndValidateInput() {
        String id       = nz(lblId.getText());
        String name     = nz(txtName.getText());
        String duration = nz(txtDuration.getText());
        String feeTxt   = nz(txtFee.getText()).replace(",", "").trim();

        resetFieldStyles();

        boolean valid = true;
        if (!name.matches(NAME_PATTERN))         { markInvalid(txtName);     valid = false; }
        if (!duration.matches(DURATION_PATTERN)) { markInvalid(txtDuration); valid = false; }
        if (!feeTxt.matches(FEE_PATTERN))        { markInvalid(txtFee);      valid = false; }

        if (!valid) return null;

        BigDecimal fee = new BigDecimal(feeTxt);
        return new CourseDTO(id, name, duration, fee);
    }

    private static String nz(String s) { return s == null ? "" : s.trim(); }

    // ---------- Optional: seed the 5 sample rows ----------

    @SuppressWarnings("unused")
    private void seedDefaultsIfEmpty() throws Exception {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Long count = session.createQuery("select count(c) from Course c", Long.class).uniqueResult();
            if (count != null && count > 0) return;
        }

        saveCourse(new CourseDTO("C1001", "Basic Learner Program",     "12 weeks", new BigDecimal("50000.00")));
        saveCourse(new CourseDTO("C1002", "Advanced Defensive Driving", "8 weeks",  new BigDecimal("65000.00")));
        saveCourse(new CourseDTO("C1003", "Motorcycle License Training","16 weeks", new BigDecimal("75000.00")));
        saveCourse(new CourseDTO("C1004", "Heavy Vehicle Training",     "6 months", new BigDecimal("150000.00")));
        saveCourse(new CourseDTO("C1005", "Refresher Driving Course",   "3 months", new BigDecimal("30000.00")));
    }
}
